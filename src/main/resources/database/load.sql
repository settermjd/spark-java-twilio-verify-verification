CREATE TABLE IF NOT EXISTS user (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    username TEXT NOT NULL,
    first_name TEXT NULL,
    last_name TEXT NULL,
    email_address TEXT NOT NULL
);

CREATE UNIQUE INDEX IF NOT EXISTS idx_usr_uniq_username
    ON user(username);

CREATE UNIQUE INDEX IF NOT EXISTS idx_usr_uniq_email
    ON user(email_address);

CREATE INDEX IF NOT EXISTS idx_usr_username_first_last_email
    ON user(username, first_name, last_name, email_address);

INSERT INTO user(username, first_name, last_name, email_address)
VALUES
    ("settermjd", "Matthew", "Setter", "matthew@matthewsetter.com"),
    ("firstuser", "First", "User", "firstuser@example.org"),
    ("seconduser", "First", "User", "seconduser@example.org"),
    ("thirduser", "First", "User", "thirduser@example.org")
;