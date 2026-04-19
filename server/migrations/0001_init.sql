create table if not exists good_habit_logs (
    record_date date primary key,
    count integer not null default 0,
    updated_at timestamptz not null default now(),
    deleted_at timestamptz null
);

create index if not exists idx_good_habit_logs_updated_at on good_habit_logs(updated_at);
