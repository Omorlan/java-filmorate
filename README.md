# java-filmorate
Template repository for Filmorate project.
```mermaid
erDiagram
    USERS {
        user_id integer PK
        user_name varchar(128)
        user_login varchar(40)
        user_email varchar(128)
        user_birthday timestamp
    }

    FILMS {
        film_id integer PK
        film_name varchar(256)
        film_description varchar(256)
        film_duration integer
        film_releaseDate timestamp
        mpa_id integer FK
    }

    FRIENDSHIP {
        accepting_user_id integer PK, FK
        requesting_user_id integer PK, FK
        status varchar(40)
    }

    LIKES {
        film_id integer PK
        user_id integer PK
    }

    GENRES {
        genre_id integer PK
        genre_name varchar(128)
    }

    MPA {
        mpa_id integer PK
        mpa_name varchar(40)
    }

    FILM_GENRES {
        film_id integer PK, FK
        genre_id integer PK, FK
    }

    REVIEWS {
        review_id integer PK
        content varchar(1024)
        is_positive boolean
        user_id integer FK
        film_id integer FK
        useful integer
    }

    REVIEW_LIKES {
        review_id integer PK, FK
        user_id integer PK, FK
        is_useful boolean
    }

    USERS ||--|{ LIKES: contains
    FILMS ||--|{ LIKES: contains
    USERS ||--|{ FRIENDSHIP: contains
    FILMS ||--|{ FILM_GENRES: contains
    FILM_GENRES ||--|{ GENRES: contains
    FILMS ||--|{ MPA: contains
    USERS ||--|{ REVIEWS: creates
    FILMS ||--|{ REVIEWS: contains
    REVIEWS ||--|{ REVIEW_LIKES: receives
    USERS ||--|{ REVIEW_LIKES: likes

```
