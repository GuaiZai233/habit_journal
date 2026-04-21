package main

import (
	"context"
	"database/sql"
	"encoding/json"
	"log"
	"net/http"
	"os"
	"os/signal"
	"regexp"
	"syscall"
	"time"

	"github.com/go-chi/chi/v5"
	"github.com/go-chi/chi/v5/middleware"
	_ "github.com/jackc/pgx/v5/stdlib"
)

var dateRegexp = regexp.MustCompile(`^\d{4}-\d{2}-\d{2}$`)

type logDTO struct {
	RecordDate string `json:"record_date"`
	Count      int    `json:"count"`
	UpdatedAt  string `json:"updated_at"`
}

type syncPushRequest struct {
	Logs []logDTO `json:"logs"`
}

type syncPullResponse struct {
	Logs       []logDTO `json:"logs"`
	ServerTime string   `json:"server_time"`
}

func main() {
	addr := env("APP_ADDR", ":8080")
	databaseURL := env("DATABASE_URL", "postgres://habit:habit@localhost:5432/habit?sslmode=disable")

	db, err := sql.Open("pgx", databaseURL)
	if err != nil {
		log.Fatalf("open db failed: %v", err)
	}
	defer db.Close()

	if err := db.PingContext(context.Background()); err != nil {
		log.Fatalf("ping db failed: %v", err)
	}

	r := chi.NewRouter()
	r.Use(middleware.Recoverer)
	r.Use(middleware.RealIP)
	r.Use(middleware.RequestID)

	r.Route("/api/v1", func(api chi.Router) {
		api.Get("/health", func(w http.ResponseWriter, r *http.Request) {
			writeJSON(w, http.StatusOK, map[string]string{"status": "ok"})
		})

		api.Put("/logs/{date}", func(w http.ResponseWriter, r *http.Request) {
			recordDate := chi.URLParam(r, "date")
			if !dateRegexp.MatchString(recordDate) {
				writeJSON(w, http.StatusBadRequest, map[string]string{"error": "invalid date, use yyyy-mm-dd"})
				return
			}

			var req struct {
				CountDelta int `json:"count_delta"`
			}
			_ = json.NewDecoder(r.Body).Decode(&req)
			if req.CountDelta <= 0 {
				req.CountDelta = 1
			}

			var count int
			var updatedAt time.Time
			err := db.QueryRowContext(r.Context(), `
				insert into good_habit_logs (record_date, count, updated_at)
				values ($1, $2, now())
				on conflict (record_date) do update set
					count = good_habit_logs.count + excluded.count,
					updated_at = now()
				returning count, updated_at
			`, recordDate, req.CountDelta).Scan(&count, &updatedAt)
			if err != nil {
				writeJSON(w, http.StatusInternalServerError, map[string]string{"error": err.Error()})
				return
			}
			writeJSON(w, http.StatusOK, logDTO{RecordDate: recordDate, Count: count, UpdatedAt: updatedAt.UTC().Format(time.RFC3339)})
		})

		api.Get("/logs", func(w http.ResponseWriter, r *http.Request) {
			from := r.URL.Query().Get("from")
			to := r.URL.Query().Get("to")
			if from == "" || to == "" {
				now := time.Now().UTC()
				monthStart := time.Date(now.Year(), now.Month(), 1, 0, 0, 0, 0, time.UTC)
				from = monthStart.Format("2006-01-02")
				to = monthStart.AddDate(0, 1, -1).Format("2006-01-02")
			}

			rows, err := db.QueryContext(r.Context(), `
				select record_date::text, count, updated_at
				from good_habit_logs
				where record_date between $1 and $2 and deleted_at is null
				order by record_date asc
			`, from, to)
			if err != nil {
				writeJSON(w, http.StatusInternalServerError, map[string]string{"error": err.Error()})
				return
			}
			defer rows.Close()

			result := make([]logDTO, 0)
			for rows.Next() {
				var item logDTO
				var updatedAt time.Time
				if err := rows.Scan(&item.RecordDate, &item.Count, &updatedAt); err != nil {
					writeJSON(w, http.StatusInternalServerError, map[string]string{"error": err.Error()})
					return
				}
				item.UpdatedAt = updatedAt.UTC().Format(time.RFC3339)
				result = append(result, item)
			}
			writeJSON(w, http.StatusOK, map[string][]logDTO{"logs": result})
		})

		api.Post("/sync/push", func(w http.ResponseWriter, r *http.Request) {
			var req syncPushRequest
			if err := json.NewDecoder(r.Body).Decode(&req); err != nil {
				writeJSON(w, http.StatusBadRequest, map[string]string{"error": "invalid json"})
				return
			}
			merged := 0
			for _, item := range req.Logs {
				if !dateRegexp.MatchString(item.RecordDate) {
					continue
				}
				updatedAt, err := time.Parse(time.RFC3339, item.UpdatedAt)
				if err != nil {
					continue
				}
				_, err = db.ExecContext(r.Context(), `
					insert into good_habit_logs (record_date, count, updated_at)
					values ($1, $2, $3)
					on conflict (record_date) do update set
						count = excluded.count,
						updated_at = excluded.updated_at
					where good_habit_logs.updated_at <= excluded.updated_at
				`, item.RecordDate, item.Count, updatedAt.UTC())
				if err == nil {
					merged++
				}
			}
			writeJSON(w, http.StatusOK, map[string]int{"merged": merged})
		})

		api.Get("/sync/pull", func(w http.ResponseWriter, r *http.Request) {
			sinceRaw := r.URL.Query().Get("since")
			if sinceRaw == "" {
				sinceRaw = "1970-01-01T00:00:00Z"
			}
			since, err := time.Parse(time.RFC3339, sinceRaw)
			if err != nil {
				writeJSON(w, http.StatusBadRequest, map[string]string{"error": "since must be RFC3339"})
				return
			}

			rows, err := db.QueryContext(r.Context(), `
				select record_date::text, count, updated_at
				from good_habit_logs
				where updated_at > $1 and deleted_at is null
				order by updated_at asc
			`, since)
			if err != nil {
				writeJSON(w, http.StatusInternalServerError, map[string]string{"error": err.Error()})
				return
			}
			defer rows.Close()

			logs := make([]logDTO, 0)
			for rows.Next() {
				var item logDTO
				var updatedAt time.Time
				if err := rows.Scan(&item.RecordDate, &item.Count, &updatedAt); err != nil {
					writeJSON(w, http.StatusInternalServerError, map[string]string{"error": err.Error()})
					return
				}
				item.UpdatedAt = updatedAt.UTC().Format(time.RFC3339)
				logs = append(logs, item)
			}

			writeJSON(w, http.StatusOK, syncPullResponse{
				Logs:       logs,
				ServerTime: time.Now().UTC().Format(time.RFC3339),
			})
		})
	})

	srv := &http.Server{
		Addr:         addr,
		Handler:      r,
		ReadTimeout:  10 * time.Second,
		WriteTimeout: 10 * time.Second,
		IdleTimeout:  60 * time.Second,
	}

	go func() {
		log.Printf("server listening on %s", addr)
		if err := srv.ListenAndServe(); err != nil && err != http.ErrServerClosed {
			log.Fatalf("listen failed: %v", err)
		}
	}()

	quit := make(chan os.Signal, 1)
	signal.Notify(quit, syscall.SIGINT, syscall.SIGTERM)
	<-quit

	ctx, cancel := context.WithTimeout(context.Background(), 10*time.Second)
	defer cancel()
	_ = srv.Shutdown(ctx)
}

func env(key, fallback string) string {
	value := os.Getenv(key)
	if value == "" {
		return fallback
	}
	return value
}

func writeJSON(w http.ResponseWriter, status int, body interface{}) {
	w.Header().Set("Content-Type", "application/json; charset=utf-8")
	w.WriteHeader(status)
	_ = json.NewEncoder(w).Encode(body)
}
