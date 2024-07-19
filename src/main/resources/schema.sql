DROP TABLE IF EXISTS users, mpa, genres, directors, film_genres, films, film_directors, friendship, likes;

CREATE TABLE IF NOT EXISTS mpa (
    mpa_id INTEGER NOT NULL GENERATED BY DEFAULT AS IDENTITY,
    mpa_name VARCHAR(40),
    CONSTRAINT mpa_id_pk PRIMARY KEY (mpa_id)
);
CREATE TABLE IF NOT EXISTS genres (
    genre_id INTEGER NOT NULL GENERATED BY DEFAULT AS IDENTITY,
    genre_name VARCHAR(128),
    CONSTRAINT genre_id_pk PRIMARY KEY (genre_id)
);
CREATE TABLE IF NOT EXISTS directors (
    director_id INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    director_name VARCHAR(128) NOT NULL
);
CREATE TABLE IF NOT EXISTS users (
  user_id INTEGER NOT NULL GENERATED BY DEFAULT AS IDENTITY,
  user_name VARCHAR(128),
  user_login VARCHAR(40),
  user_email VARCHAR(128),
  user_birthday TIMESTAMP,
  CONSTRAINT user_id_pk PRIMARY KEY (user_id),
  CONSTRAINT unique_email UNIQUE (user_email)
);

CREATE TABLE IF NOT EXISTS films (
    film_id INTEGER NOT NULL GENERATED BY DEFAULT AS IDENTITY,
    film_name VARCHAR(256),
    film_description VARCHAR(256),
    film_duration INTEGER,
    film_releaseDate TIMESTAMP,
    mpa_id INTEGER REFERENCES mpa (mpa_id),
    CONSTRAINT film_id_pk PRIMARY KEY (film_id)
);

CREATE TABLE IF NOT EXISTS likes (
    film_id INTEGER REFERENCES films (film_id) ON DELETE CASCADE,
    user_id INTEGER REFERENCES users (user_id) ON DELETE CASCADE,
    CONSTRAINT likes_pk PRIMARY KEY (film_id, user_id)
);

CREATE TABLE IF NOT EXISTS film_directors (
    film_id INTEGER REFERENCES films (film_id) ON DELETE CASCADE,
    director_id INTEGER REFERENCES directors (director_id) ON DELETE CASCADE,
    PRIMARY KEY (film_id, director_id)
);

CREATE TABLE IF NOT EXISTS film_genres (
  film_id INTEGER NOT NULL,
  genre_id INTEGER NOT NULL,
  CONSTRAINT film_genres_pk PRIMARY KEY (film_id, genre_id),
  CONSTRAINT fk_film FOREIGN KEY (film_id) REFERENCES films (film_id) ON DELETE CASCADE,
  CONSTRAINT fk_genre FOREIGN KEY (genre_id) REFERENCES genres (genre_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS friendship (
  accepting_user_id INTEGER REFERENCES users (user_id) ON DELETE CASCADE,
  requesting_user_id INTEGER REFERENCES users (user_id) ON DELETE CASCADE,
  status VARCHAR(40),
  CONSTRAINT friendship_pk PRIMARY KEY (accepting_user_id, requesting_user_id)
);



