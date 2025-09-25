package info.jab.control;

import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * A discriminated union that encapsulates a successful outcome with a value of type T
 * or a failure with an arbitrary Throwable exception.
 *
 * This class is inspired by Kotlin's Result class and provides similar functionality
 * for functional error handling in Java.
 *
 * @param <T> the type of the successful value
 */
public final class Result<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    private final T value;
    private final Throwable exception;
    private final boolean isSuccess;

    /**
     * Private constructor for successful results.
     */
    private Result(T value) {
        this.value = value;
        this.exception = null;
        this.isSuccess = true;
    }

    /**
     * Private constructor for failed results.
     */
    private Result(Throwable exception) {
        this.value = null;
        this.exception = Objects.requireNonNull(exception, "Exception cannot be null");
        this.isSuccess = false;
    }

    /**
     * Returns a successful Result containing the specified value.
     *
     * @param <T> the type of the value
     * @param value the successful value
     * @return a successful Result
     */
    public static <T> Result<T> success(T value) {
        return new Result<>(value);
    }

    /**
     * Returns a failed Result containing the specified exception.
     *
     * @param <T> the type that would have been returned on success
     * @param exception the exception representing the failure
     * @return a failed Result
     */
    public static <T> Result<T> failure(Throwable exception) {
        return new Result<>(exception);
    }

    /**
     * Returns true if this instance represents a successful outcome.
     * In this case isFailure returns false.
     *
     * @return true if successful, false otherwise
     */
    public boolean isSuccess() {
        return isSuccess;
    }

    /**
     * Returns true if this instance represents a failed outcome.
     * In this case isSuccess returns false.
     *
     * @return true if failed, false otherwise
     */
    public boolean isFailure() {
        return !isSuccess;
    }

    /**
     * Returns the encapsulated value if this instance represents success
     * or null if it is failure.
     *
     * @return the value or null
     */
    public T getOrNull() {
        return isSuccess ? value : null;
    }

    /**
     * Returns the encapsulated Throwable exception if this instance represents failure
     * or null if it is success.
     *
     * @return the exception or null
     */
    public Throwable exceptionOrNull() {
        return isSuccess ? null : exception;
    }

    /**
     * Returns the encapsulated value if this instance represents success
     * or throws the encapsulated Throwable exception if it is failure.
     *
     * @return the successful value
     * @throws Throwable the encapsulated exception if this is a failure
     */
    public T getOrThrow() throws Throwable {
        if (isSuccess) {
            return value;
        } else {
            throw exception;
        }
    }

    /**
     * Returns the encapsulated value if this instance represents success
     * or the defaultValue if it is failure.
     *
     * @param defaultValue the default value to return on failure
     * @return the value or default value
     */
    public T getOrDefault(T defaultValue) {
        return isSuccess ? value : defaultValue;
    }

    /**
     * Returns the encapsulated value if this instance represents success
     * or the result of onFailure function for the encapsulated Throwable exception if it is failure.
     *
     * Uses PECS principle: Function<? super Throwable, ? extends T> for maximum flexibility.
     *
     * @param onFailure function to compute a value from the exception
     * @return the value or the result of onFailure
     */
    public T getOrElse(Function<? super Throwable, ? extends T> onFailure) {
        return isSuccess ? value : onFailure.apply(exception);
    }

    /**
     * Returns the result of onSuccess for the encapsulated value if this instance represents success
     * or the result of onFailure function for the encapsulated Throwable exception if it is failure.
     *
     * Uses PECS principle for both function parameters for maximum flexibility.
     *
     * @param <R> the return type
     * @param onSuccess function to apply to successful value
     * @param onFailure function to apply to failure exception
     * @return the result of either function
     */
    public <R> R fold(Function<? super T, ? extends R> onSuccess, Function<? super Throwable, ? extends R> onFailure) {
        return isSuccess ? onSuccess.apply(value) : onFailure.apply(exception);
    }

    /**
     * Returns the encapsulated result of the given transform function applied to the encapsulated value
     * if this instance represents success or the original encapsulated Throwable exception if it is failure.
     *
     * Uses PECS principle: Function<? super T, ? extends R> for maximum flexibility.
     *
     * @param <R> the type of the transformed value
     * @param transform function to transform the successful value
     * @return a new Result with the transformed value or the original failure
     */
    public <R> Result<R> map(Function<? super T, ? extends R> transform) {
        if (isSuccess) {
            try {
                return Result.success(transform.apply(value));
            } catch (Exception e) {
                return Result.failure(e);
            }
        } else {
            return Result.failure(exception);
        }
    }

    /**
     * Returns the encapsulated result of the given transform function applied to the encapsulated value
     * if this instance represents success or the original encapsulated Throwable exception if it is failure.
     *
     * Note: Any exception thrown by transform function is caught and encapsulated as a failure.
     * Uses PECS principle for maximum API flexibility.
     *
     * @param <R> the type of the transformed value
     * @param transform function to transform the successful value
     * @return a new Result with the transformed value or a failure
     */
    public <R> Result<R> mapCatching(Function<? super T, ? extends R> transform) {
        if (isSuccess) {
            try {
                return Result.success(transform.apply(value));
            } catch (Throwable e) {
                return Result.failure(e);
            }
        } else {
            return Result.failure(exception);
        }
    }

    /**
     * Performs the given action on the encapsulated value if this instance represents success.
     * Returns the original Result unchanged.
     *
     * Uses PECS principle: Consumer<? super T> for maximum flexibility.
     *
     * @param action action to perform on successful value
     * @return this Result unchanged
     */
    public Result<T> onSuccess(Consumer<? super T> action) {
        if (isSuccess) {
            action.accept(value);
        }
        return this;
    }

    /**
     * Performs the given action on the encapsulated Throwable exception if this instance represents failure.
     * Returns the original Result unchanged.
     *
     * Uses PECS principle: Consumer<? super Throwable> for maximum flexibility.
     *
     * @param action action to perform on failure exception
     * @return this Result unchanged
     */
    public Result<T> onFailure(Consumer<? super Throwable> action) {
        if (!isSuccess) {
            action.accept(exception);
        }
        return this;
    }

    /**
     * Returns the encapsulated result of the given transform function applied to the encapsulated Throwable exception
     * if this instance represents failure or the original encapsulated value if it is success.
     *
     * Uses PECS principle: Function<? super Throwable, ? extends T> for maximum flexibility.
     *
     * @param transform function to transform the failure exception into a successful value
     * @return a new Result with the recovered value or the original success
     */
    public Result<T> recover(Function<? super Throwable, ? extends T> transform) {
        if (isSuccess) {
            return this;
        } else {
            try {
                return Result.success(transform.apply(exception));
            } catch (Exception e) {
                return Result.failure(e);
            }
        }
    }

    /**
     * Returns the encapsulated result of the given transform function applied to the encapsulated Throwable exception
     * if this instance represents failure or the original encapsulated value if it is success.
     *
     * Note: Any exception thrown by transform function is caught and encapsulated as a failure.
     * Uses PECS principle for maximum API flexibility.
     *
     * @param transform function to transform the failure exception into a successful value
     * @return a new Result with the recovered value or a failure
     */
    public Result<T> recoverCatching(Function<? super Throwable, ? extends T> transform) {
        if (isSuccess) {
            return this;
        } else {
            try {
                return Result.success(transform.apply(exception));
            } catch (Throwable e) {
                return Result.failure(e);
            }
        }
    }

    @Override
    public String toString() {
        if (isSuccess) {
            return "Success(" + value + ")";
        } else {
            return "Failure(" + exception + ")";
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        Result<?> result = (Result<?>) obj;
        return isSuccess == result.isSuccess &&
               Objects.equals(value, result.value) &&
               Objects.equals(exception, result.exception);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value, exception, isSuccess);
    }

    // ========== Advanced Generic Methods ==========

    /**
     * Returns a successful Result containing the specified value if the predicate returns true,
     * otherwise returns a failure with the provided exception.
     *
     * Uses bounded type parameter to ensure exception is a Throwable.
     *
     * @param <T> the type of the value
     * @param <E> the type of the exception, must extend Throwable
     * @param value the value to test
     * @param predicate the predicate to test the value
     * @param exceptionSupplier supplier for the exception if predicate fails
     * @return a Result based on the predicate evaluation
     */
    public static <T, E extends Throwable> Result<T> fromPredicate(
            T value,
            Predicate<? super T> predicate,
            Supplier<? extends E> exceptionSupplier) {
        return predicate.test(value) ? success(value) : failure(exceptionSupplier.get());
    }

    /**
     * Returns a successful Result containing the specified value if it's not null,
     * otherwise returns a failure with NullPointerException.
     *
     * @param <T> the type of the value
     * @param value the nullable value
     * @return a Result containing the value or a failure
     */
    public static <T> Result<T> fromNullable(T value) {
        return value != null ? success(value) : failure(new NullPointerException("Value is null"));
    }

    /**
     * Returns a successful Result containing the specified value if it's not null,
     * otherwise returns a failure with the provided exception.
     *
     * @param <T> the type of the value
     * @param <E> the type of the exception, must extend Throwable
     * @param value the nullable value
     * @param exceptionSupplier supplier for the exception if value is null
     * @return a Result containing the value or a failure
     */
    public static <T, E extends Throwable> Result<T> fromNullable(
            T value,
            Supplier<? extends E> exceptionSupplier) {
        return value != null ? success(value) : failure(exceptionSupplier.get());
    }

    /**
     * Transforms a supplier that might throw into a Result.
     * Uses bounded type parameter to ensure exception handling.
     *
     * @param <T> the type of the value
     * @param supplier the supplier that might throw
     * @return a Result containing the value or the caught exception
     */
    public static <T> Result<T> fromSupplier(Supplier<? extends T> supplier) {
        try {
            return success(supplier.get());
        } catch (Throwable e) {
            return failure(e);
        }
    }

    /**
     * Executes the given supplier and catches any exceptions that may be thrown.
     * This is a convenience method for wrapping potentially throwing operations in a Result.
     *
     * This method is similar to Kotlin's runCatching function and provides a clean way
     * to handle exceptions functionally.
     *
     * @param <T> the type of the value returned by the supplier
     * @param supplier the supplier function to execute
     * @return a Result containing the successful value or the caught exception
     */
    public static <T> Result<T> runCatching(Supplier<? extends T> supplier) {
        try {
            return success(supplier.get());
        } catch (Throwable e) {
            return failure(e);
        }
    }

    /**
     * Executes the given callable and catches any exceptions that may be thrown, including checked exceptions.
     * This is a specialized version of runCatching that can handle methods that throw checked exceptions.
     *
     * This method uses a functional interface that allows checked exceptions to be thrown,
     * making it perfect for wrapping API calls or other operations that throw checked exceptions.
     *
     * @param <T> the type of the value returned by the callable
     * @param callable the callable function to execute that may throw checked exceptions
     * @return a Result containing the successful value or the caught exception
     */
    public static <T> Result<T> runCatching(CheckedSupplier<T> callable) {
        try {
            return success(callable.get());
        } catch (Throwable e) {
            return failure(e);
        }
    }

    /**
     * Functional interface that allows suppliers to throw checked exceptions.
     * This is used with the runCatching method to handle operations that may throw checked exceptions.
     *
     * @param <T> the type of results supplied by this supplier
     */
    @FunctionalInterface
    public interface CheckedSupplier<T> {
        /**
         * Gets a result, potentially throwing a checked exception.
         *
         * @return a result
         * @throws Exception if unable to produce a result
         */
        T get() throws Exception;
    }

    /**
     * Flat map operation for monadic composition.
     * Applies the transform function if this is a success, otherwise returns the failure.
     *
     * Uses PECS principle for the transform function parameter.
     *
     * @param <R> the type of the result value
     * @param transform function that transforms the value to another Result
     * @return a new Result from the transformation or the original failure
     */
    public <R> Result<R> flatMap(Function<? super T, ? extends Result<? extends R>> transform) {
        if (isSuccess) {
            try {
                Result<? extends R> result = transform.apply(value);
                return result.isSuccess() ? success(result.value) : failure(result.exception);
            } catch (Exception e) {
                return failure(e);
            }
        } else {
            return failure(exception);
        }
    }

    /**
     * Flat map operation that catches exceptions.
     * Similar to flatMap but catches any exceptions thrown by the transform function.
     *
     * @param <R> the type of the result value
     * @param transform function that transforms the value to another Result
     * @return a new Result from the transformation or a failure
     */
    public <R> Result<R> flatMapCatching(Function<? super T, ? extends Result<? extends R>> transform) {
        if (isSuccess) {
            try {
                Result<? extends R> result = transform.apply(value);
                return result.isSuccess() ? success(result.value) : failure(result.exception);
            } catch (Throwable e) {
                return failure(e);
            }
        } else {
            return failure(exception);
        }
    }

    /**
     * Filters the Result based on a predicate.
     * If this is a success and the predicate passes, returns this Result.
     * If this is a success but the predicate fails, returns a failure with the provided exception.
     * If this is already a failure, returns this Result.
     *
     * @param <E> the type of the exception, must extend Throwable
     * @param predicate the predicate to test the value
     * @param exceptionSupplier supplier for the exception if predicate fails
     * @return a filtered Result
     */
    public <E extends Throwable> Result<T> filter(
            Predicate<? super T> predicate,
            Supplier<? extends E> exceptionSupplier) {
        if (isSuccess) {
            return predicate.test(value) ? this : failure(exceptionSupplier.get());
        } else {
            return this;
        }
    }

    /**
     * Converts this Result to an Optional.
     * Returns Optional.of(value) if success, Optional.empty() if failure.
     *
     * @return an Optional containing the value or empty
     */
    public Optional<T> toOptional() {
        return isSuccess ? Optional.of(value) : Optional.empty();
    }

    /**
     * Converts this Result to a Stream.
     * Returns a stream with the value if success, empty stream if failure.
     *
     * @return a Stream containing the value or empty
     */
    public Stream<T> stream() {
        return isSuccess ? Stream.of(value) : Stream.empty();
    }

    /**
     * Returns the encapsulated value if this instance represents success
     * or the result of the supplier function if it is failure.
     *
     * Uses bounded wildcard for the supplier return type.
     *
     * @param supplier function to compute a value on failure
     * @return the value or the result of supplier
     */
    public T getOrElse(Supplier<? extends T> supplier) {
        return isSuccess ? value : supplier.get();
    }

    /**
     * Combines two Results using a binary function.
     * If both Results are successful, applies the combiner function.
     * If either Result is a failure, returns the first failure encountered.
     *
     * Uses PECS principle for all function parameters.
     *
     * @param <U> the type of the other Result's value
     * @param <R> the type of the combined result
     * @param other the other Result to combine with
     * @param combiner function to combine the two values
     * @return a Result containing the combined value or the first failure
     */
    public <U, R> Result<R> combine(
            Result<? extends U> other,
            java.util.function.BiFunction<? super T, ? super U, ? extends R> combiner) {
        if (this.isSuccess && other.isSuccess) {
            try {
                return success(combiner.apply(this.value, other.value));
            } catch (Exception e) {
                return failure(e);
            }
        } else if (!this.isSuccess) {
            return failure(this.exception);
        } else {
            return failure(other.exception);
        }
    }

    /**
     * Creates a Result from a boolean condition.
     * If condition is true, returns success with the provided value.
     * If condition is false, returns failure with the provided exception.
     *
     * @param <T> the type of the value
     * @param <E> the type of the exception, must extend Throwable
     * @param condition the boolean condition to test
     * @param value the value to return on success
     * @param exceptionSupplier supplier for the exception on failure
     * @return a Result based on the condition
     */
    public static <T, E extends Throwable> Result<T> fromCondition(
            boolean condition,
            T value,
            Supplier<? extends E> exceptionSupplier) {
        return condition ? success(value) : failure(exceptionSupplier.get());
    }

    /**
     * Utility method for wildcard capture when needed.
     * This is a helper method that can be used internally for type-safe operations.
     *
     * @param <T> the captured type
     * @param result the result with wildcard type
     * @return the same result with captured type
     */
    @SuppressWarnings("unchecked")
    private static <T> Result<T> captureWildcard(Result<? extends T> result) {
        return (Result<T>) result;
    }

    // ========== Advanced Utility Methods ==========

    /**
     * Applies a side effect function to the value if this is a success, then returns the original Result.
     * This is useful for logging, debugging, or other side effects without changing the Result.
     *
     * Uses PECS principle for the action parameter.
     *
     * @param action the side effect function to apply
     * @return this Result unchanged
     */
    public Result<T> peek(Consumer<? super T> action) {
        if (isSuccess) {
            action.accept(value);
        }
        return this;
    }

    /**
     * Applies a side effect function to the exception if this is a failure, then returns the original Result.
     * This is useful for logging, debugging, or other side effects without changing the Result.
     *
     * @param action the side effect function to apply to the exception
     * @return this Result unchanged
     */
    public Result<T> peekError(Consumer<? super Throwable> action) {
        if (!isSuccess) {
            action.accept(exception);
        }
        return this;
    }

    /**
     * Creates a Result from an Optional, using the provided exception supplier for empty Optionals.
     * Demonstrates integration with other generic types.
     *
     * @param <T> the type of the Optional value
     * @param <E> the type of the exception, must extend Throwable
     * @param optional the Optional to convert
     * @param exceptionSupplier supplier for the exception if Optional is empty
     * @return a Result containing the Optional's value or a failure
     */
    public static <T, E extends Throwable> Result<T> fromOptional(
            Optional<? extends T> optional,
            Supplier<? extends E> exceptionSupplier) {
        return optional.isPresent() ? success(optional.get()) : failure(exceptionSupplier.get());
    }

    /**
     * Collects multiple Results into a single Result containing a list of all successful values.
     * If any Result is a failure, returns the first failure encountered.
     *
     * This demonstrates advanced generic collection operations with wildcards and @SafeVarargs.
     *
     * @param <T> the type of the values
     * @param results the collection of Results to combine
     * @return a Result containing a list of all successful values, or the first failure
     */
    @SafeVarargs
    public static <T> Result<java.util.List<T>> sequence(Result<? extends T>... results) {
        java.util.List<T> values = new java.util.ArrayList<>();
        for (Result<? extends T> result : results) {
            if (result.isSuccess()) {
                values.add(result.value);
            } else {
                return failure(result.exception);
            }
        }
        return success(java.util.Collections.unmodifiableList(values));
    }

    /**
     * Custom serialization method to ensure proper handling of generic types.
     * This method is called during serialization to write the object state.
     *
     * @param out the ObjectOutputStream to write to
     * @throws java.io.IOException if an I/O error occurs
     */
    private void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {
        out.defaultWriteObject();
    }

    /**
     * Custom deserialization method to ensure proper handling of generic types.
     * This method is called during deserialization to read the object state.
     *
     * @param in the ObjectInputStream to read from
     * @throws java.io.IOException if an I/O error occurs
     * @throws ClassNotFoundException if a class cannot be found
     */
    private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, ClassNotFoundException {
        in.defaultReadObject();
        // Additional validation could be added here if needed
        if (isSuccess && value == null && exception != null) {
            throw new java.io.InvalidObjectException("Inconsistent state: success flag true but value is null and exception is not null");
        }
        if (!isSuccess && exception == null) {
            throw new java.io.InvalidObjectException("Inconsistent state: success flag false but exception is null");
        }
    }
}
