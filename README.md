# java-filmorate
Template repository for Filmorate project.
```mermaid
erDiagram
    USERS {
        user_id bigint PK
        user_name varchar(128)
        user_login varchar(40)
        user_email varchar(128)
        user_birthday timestamp
    }

    FILMS {
        film_id bigint PK
        film_name varchar(256)
        film_description varchar(256)
        film_duration bigint
        film_releaseDate timestamp
        mpa_id bigint FK
    }

    FRIENDSHIP {
        accepting_user_id bigint PK, FK
        requesting_user_id bigint PK, FK
        status varchar(40)
    }

    LIKES {
        film_id bigint PK
        user_id bigint PK
    }

    GENRES {
        genre_id bigint PK
        genre_name varchar(128)
    }

    MPA {
        mpa_id bigint PK
        mpa_name varchar(40)
    }

    FILM_GENRES {
        film_id bigint PK, FK
        genre_id bigint PK, FK
    }

    REVIEWS {
        review_id bigint PK
        content varchar(1024)
        is_positive boolean
        user_id bigint FK
        film_id bigint FK
        useful bigint
    }

    REVIEW_LIKES {
        review_id bigint PK, FK
        user_id bigint PK, FK
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
