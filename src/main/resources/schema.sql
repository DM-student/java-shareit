CREATE TABLE IF NOT EXISTS users (
        id BIGINT NOT NULL PRIMARY KEY,
        email VARCHAR(320) NOT NULL,
        name VARCHAR(64) NOT NULL
);

CREATE TABLE IF NOT EXISTS items (
        id BIGINT NOT NULL PRIMARY KEY,
        name VARCHAR(32) NOT NULL,
        description VARCHAR(378) NOT NULL,
        available BIT NOT NULL
);

CREATE TABLE IF NOT EXISTS users_to_items(
	user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
	item_id BIGINT NOT NULL REFERENCES items(id) ON DELETE CASCADE,
	CONSTRAINT unique_users_to_items_entry UNIQUE (user_id, item_id)
);