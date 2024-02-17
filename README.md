# Unwrap Either
A test to make chained Either unwrapping barable in java.

![GitHub Release](https://img.shields.io/github/v/release/OpenResult/unwrap-either?include_prereleases)

## Model business logic with Either
A popular way to make consise and easily testable steps in your business logic is to use Either's. It could looks something like this.
```java
    Either<ServiceOneError, Integer> findUserId(String cookie) {
        if (cookie.startsWith("valid")) {
            return Either.right(cookie.length());
        } else {
            return Either.left(ServiceOneError.UserIdNotFound);
        }
    }
```

## Unwrapping of Either's
Once we have this collection of nice steps of business logic we want to chain them together. Here it
is important that we can do this in a consise and maintainable way. This code is where we have the
overview of our business logic.

### Unwrapping in java
In java we have a few options how to chain our business logic of Eithers together.
Best way is probably to use nested `flatMap` but as the number of steps grow this starts looking crazy.
```java
    private Either<ServiceOneError, String> execute(String cookie) {
        return serviceOne.findUserId(cookie)
            .flatMap(id -> serviceOne.findUserName(id));
    }
```

### Unwrapping in scala
In scala there is a nice language structure for this perticular problem. As you can se it looks
very nice and if you add five more steps it would still look nice.

```scala
def execute(cookie: String) =
    for {
        id <- findUserId(cookie)
        name <- findUserName(id)
    } yield name
```

### Unwrapping in Rust
In Rust the `Either` equivalent is `Result`. Rust has no for comprehension but it has the neat macro `?`. **Notice** the suttle question mark in the function call to `find_user_id` below. The questionmark can be used whenever the enclosing function has the same Err type in the Result return type. You would use it for all steps except the last one.
```rust
pub fn execute(cookie: &str) -> Result<String, &str> {
    let id = find_user_id(cookie)?;
    let name = find_user_name(id);
    name
}
```

## What does it look like with unwrap-either
In this library idea we use some Annotation processing and shenanigans to be able to
unwrap our Either functions like this.

Annotate class with functions to wrap like this. Like the Rust question mark operator we
need to have same error type in the Either.
```java
@Unwrapped(ServiceOneError.class)
public class ServiceOne {
    public Either<ServiceOneError, Integer> findUserId(String cookie) {
        ...
    }
    public Either<ServiceOneError, String> findUserName(Integer id) {
        ...
    }
}
```

And then you can chain your unwrapped business logic in similar clear way as Rust.
```java
    public String apply(String t) {
        var userId = findUserId(t);
        var userName = findUserName(userId);
        return userName;
    }
```

## todo
- [ ] Make unwrapped functions availabe in acutal annotated class
- Create execute options:
    - [ ] executeUnchecked
    - [ ] executeChecked
    - [ ] executeEither
- [ ] Mark generated classes with `javax.annotation.processing.Generated`
