CREATE TABLE IF NOT EXISTS users (
    id INTEGER GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    name VARCHAR(250) NOT NULL,
    email VARCHAR(254) NOT NULL,
    CONSTRAINT pk_user PRIMARY KEY (id),
    CONSTRAINT UQ_USER_EMAIL UNIQUE (email)
);

CREATE TABLE IF NOT EXISTS categories (
    id INTEGER GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    name VARCHAR(50) NOT NULL,
    CONSTRAINT pk_category PRIMARY KEY (id),
    CONSTRAINT UQ_CATEGORY_NAME UNIQUE (name)
);

CREATE TABLE IF NOT EXISTS locations (
    id INTEGER GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    lat FLOAT NOT NULL,
    lon FLOAT NOT NULL,
    CONSTRAINT pk_location PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS events (
    id INTEGER GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    annotation VARCHAR(2000) NOT NULL,
    category_id INTEGER NOT NULL,
    description VARCHAR(7000) NOT NULL,
    event_date TIMESTAMP NOT NULL,
    location_id INTEGER NOT NULL,
    paid BOOLEAN NOT NULL,
    participant_limit INTEGER NOT NULL,
    request_moderation BOOLEAN NOT NULL,
    title VARCHAR(120) NOT NULL,
    created_on TIMESTAMP NOT NULL,
    published_on TIMESTAMP,
    initiator_id INTEGER NOT NULL,
    state VARCHAR(20) NOT NULL,
    CONSTRAINT pk_event PRIMARY KEY (id),
    FOREIGN KEY (initiator_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE RESTRICT,
    FOREIGN KEY (location_id) REFERENCES locations(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS requests (
    id INTEGER GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    event_id INTEGER NOT NULL,
    requester_id INTEGER NOT NULL,
    status VARCHAR(20) NOT NULL,
    created TIMESTAMP NOT NULL,
    CONSTRAINT pk_request PRIMARY KEY (id),
    FOREIGN KEY (requester_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (event_id) REFERENCES events(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS compilations (
    id INTEGER GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    pinned BOOLEAN NOT NULL,
    title VARCHAR(50) NOT NULL,
    CONSTRAINT pk_compilation PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS compilation_event (
    id INTEGER GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    compilation_id INTEGER NOT NULL,
    event_id INTEGER NOT NULL,
    CONSTRAINT pk_compilation_event PRIMARY KEY (id),
    FOREIGN KEY (event_id) REFERENCES events(id) ON DELETE CASCADE,
    FOREIGN KEY (compilation_id) REFERENCES compilations(id) ON DELETE CASCADE
);
