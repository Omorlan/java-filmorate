# java-filmorate
Template repository for Filmorate project.
```mermaid
erDiagram
   
    users {
        user_id integer PK
        user_name varchar(256)
        user_login varchar(40)
        user_email varchar(256)
        user_birthday timestamp
    }

    films {
        film_id integer PK
        film_name varchar(256)
        film_description varchar(256)
        film_duration integer
        film_releaseDate timestamp
        mpa_id integer FK
    }
    friendship {
        accepting_user_id integer PK, FK
        requesting_user_id integer PK, FK
        status varchar(40)
    }
    likes {
        film_id integer PK
        user_id integer PK
    }

    genres {
        genre_id integer PK
        genre_name varchar(256)
    }

    mpa {
        mpa_id integer PK
        mpa_name varchar(256)
    }

    film_genres {
        film_id integer PK, FK
        genre_id integer PK, FK
    }

    users ||--|{ likes: contains
    films ||--|{ likes: contains
    users ||--|{ friendship: contains
    films ||--|{ film_genres: contains
    film_genres ||--|{ genres: contains
    films ||--|{ mpa: contains
```