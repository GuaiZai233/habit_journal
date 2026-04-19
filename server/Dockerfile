FROM golang:1.23-alpine AS builder
WORKDIR /app
COPY go.mod ./
RUN go mod download
COPY . .
RUN CGO_ENABLED=0 GOOS=linux GOARCH=amd64 go build -o /bin/api ./server/cmd/api

FROM alpine:3.21
WORKDIR /app
COPY --from=builder /bin/api /bin/api
EXPOSE 8080
ENTRYPOINT ["/bin/api"]

