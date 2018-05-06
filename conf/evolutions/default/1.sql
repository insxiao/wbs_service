# SCHEMAS

# --- !Ups
CREATE TABLE IF NOT EXISTS USERS (
  "user_id"  BIGSERIAL PRIMARY KEY,
  "username" VARCHAR(255) UNIQUE NOT NULL,
  "gender"   CHAR(1)             NOT NULL,
  "password" VARCHAR(40)         NOT NULL,
  "email"    VARCHAR(256)        NULL,
  "birthday" TIMESTAMP           NULL,
  "avatar"   VARCHAR(256)        NULL
);

CREATE TABLE IF NOT EXISTS MICRO_BLOG (
  "blog_id"   BIGSERIAL PRIMARY KEY,
  "content"   TEXT,
  "timestamp" TIMESTAMP,
  "user_id"   BIGINT REFERENCES USERS ("user_id")
);

CREATE TABLE IF NOT EXISTS COMMENTS (
  "blog_id"    BIGINT REFERENCES MICRO_BLOG ("blog_id"),
  "user_id"    BIGINT REFERENCES USERS ("user_id"),
  "comment_id" BIGSERIAL PRIMARY KEY,
  "content"    TEXT NOT NULL,
  "stars"     INT       DEFAULT 0,
  "timestamp"  TIMESTAMP DEFAULT current_timestamp
);

CREATE TABLE IF NOT EXISTS FOLLOW (
  "user_id" BIGINT REFERENCES USERS ("user_id"),
  "follower_id" BIGINT REFERENCES USERS ("user_id"),
  "timestamp" TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  CHECK ("user_id" != "follower_id"),
  UNIQUE ("user_id", "follower_id")
)

  # --- !Downs

DROP TABLE IF EXISTS FOLLOWERS;
DROP TABLE IF EXISTS USERS CASCADE;
DROP TABLE IF EXISTS COMMENTS;
DROP TABLE IF EXISTS MICRO_BLOG CASCADE;
