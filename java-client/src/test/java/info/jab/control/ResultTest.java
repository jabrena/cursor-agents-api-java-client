package info.jab.control;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Comprehensive unit tests for the Result class following Java unit testing best practices.
 * Tests are organized using nested classes and follow the Given-When-Then structure.
 */
@DisplayName("Result Class Tests")
class ResultTest {

    private static final String TEST_VALUE = "test-value";
    private static final String ANOTHER_VALUE = "another-value";
    private static final RuntimeException TEST_EXCEPTION = new RuntimeException("Test exception");
    private static final IllegalArgumentException ILLEGAL_ARG_EXCEPTION = new IllegalArgumentException("Invalid argument");

    @Nested
    @DisplayName("Factory Methods")
    class FactoryMethods {

        @Test
        @DisplayName("Should create successful result with success() factory method")
        void should_createSuccessfulResult_when_usingSuccessFactory() {
            // Given
            String value = TEST_VALUE;

            // When
            Result<String> result = Result.success(value);

            // Then
            assertThat(result.isSuccess()).isTrue();
            assertThat(result.isFailure()).isFalse();
            assertThat(result.getOrNull()).isEqualTo(value);
            assertThat(result.exceptionOrNull()).isNull();
        }

        @Test
        @DisplayName("Should create successful result with null value")
        void should_createSuccessfulResult_when_valueIsNull() {
            // Given
            String nullValue = null;

            // When
            Result<String> result = Result.success(nullValue);

            // Then
            assertThat(result.isSuccess()).isTrue();
            assertThat(result.isFailure()).isFalse();
            assertThat(result.getOrNull()).isNull();
            assertThat(result.exceptionOrNull()).isNull();
        }

        @Test
        @DisplayName("Should create failed result with failure() factory method")
        void should_createFailedResult_when_usingFailureFactory() {
            // Given
            RuntimeException exception = TEST_EXCEPTION;

            // When
            Result<String> result = Result.failure(exception);

            // Then
            assertThat(result.isSuccess()).isFalse();
            assertThat(result.isFailure()).isTrue();
            assertThat(result.getOrNull()).isNull();
            assertThat(result.exceptionOrNull()).isEqualTo(exception);
        }

        @Test
        @DisplayName("Should throw NullPointerException when failure() is called with null exception")
        void should_throwNullPointerException_when_failureFactoryCalledWithNull() {
            // Given
            Throwable nullException = null;

            // When & Then
            assertThatThrownBy(() -> Result.failure(nullException))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("Exception cannot be null");
        }
    }

    @Nested
    @DisplayName("Basic Operations")
    class BasicOperations {

        @Test
        @DisplayName("Should return value when getOrThrow() called on successful result")
        void should_returnValue_when_getOrThrowCalledOnSuccess() throws Throwable {
            // Given
            Result<String> successResult = Result.success(TEST_VALUE);

            // When
            String value = successResult.getOrThrow();

            // Then
            assertThat(value).isEqualTo(TEST_VALUE);
        }

        @Test
        @DisplayName("Should throw exception when getOrThrow() called on failed result")
        void should_throwException_when_getOrThrowCalledOnFailure() {
            // Given
            Result<String> failureResult = Result.failure(TEST_EXCEPTION);

            // When & Then
            assertThatThrownBy(() -> failureResult.getOrThrow())
                .isEqualTo(TEST_EXCEPTION);
        }

        @Test
        @DisplayName("Should return value when getOrDefault() called on successful result")
        void should_returnValue_when_getOrDefaultCalledOnSuccess() {
            // Given
            Result<String> successResult = Result.success(TEST_VALUE);
            String defaultValue = "default";

            // When
            String value = successResult.getOrDefault(defaultValue);

            // Then
            assertThat(value).isEqualTo(TEST_VALUE);
        }

        @Test
        @DisplayName("Should return default value when getOrDefault() called on failed result")
        void should_returnDefaultValue_when_getOrDefaultCalledOnFailure() {
            // Given
            Result<String> failureResult = Result.failure(TEST_EXCEPTION);
            String defaultValue = "default";

            // When
            String value = failureResult.getOrDefault(defaultValue);

            // Then
            assertThat(value).isEqualTo(defaultValue);
        }

        @Test
        @DisplayName("Should return computed value when getOrElse() called on failed result")
        void should_returnComputedValue_when_getOrElseCalledOnFailure() {
            // Given
            Result<String> failureResult = Result.failure(TEST_EXCEPTION);
            Function<Throwable, String> onFailure = ex -> "Error: " + ex.getMessage();

            // When
            String value = failureResult.getOrElse(onFailure);

            // Then
            assertThat(value).isEqualTo("Error: Test exception");
        }

        @Test
        @DisplayName("Should return original value when getOrElse() called on successful result")
        void should_returnOriginalValue_when_getOrElseCalledOnSuccess() {
            // Given
            Result<String> successResult = Result.success(TEST_VALUE);
            Function<Throwable, String> onFailure = ex -> "Error: " + ex.getMessage();

            // When
            String value = successResult.getOrElse(onFailure);

            // Then
            assertThat(value).isEqualTo(TEST_VALUE);
        }
    }

    @Nested
    @DisplayName("Transformation Operations")
    class TransformationOperations {

        @Test
        @DisplayName("Should transform value when map() called on successful result")
        void should_transformValue_when_mapCalledOnSuccess() {
            // Given
            Result<String> successResult = Result.success(TEST_VALUE);
            Function<String, Integer> transform = String::length;

            // When
            Result<Integer> transformedResult = successResult.map(transform);

            // Then
            assertThat(transformedResult.isSuccess()).isTrue();
            assertThat(transformedResult.getOrNull()).isEqualTo(TEST_VALUE.length());
        }

        @Test
        @DisplayName("Should preserve failure when map() called on failed result")
        void should_preserveFailure_when_mapCalledOnFailure() {
            // Given
            Result<String> failureResult = Result.failure(TEST_EXCEPTION);
            Function<String, Integer> transform = String::length;

            // When
            Result<Integer> transformedResult = failureResult.map(transform);

            // Then
            assertThat(transformedResult.isFailure()).isTrue();
            assertThat(transformedResult.exceptionOrNull()).isEqualTo(TEST_EXCEPTION);
        }

        @Test
        @DisplayName("Should catch exception when map() transformation throws")
        void should_catchException_when_mapTransformationThrows() {
            // Given
            Result<String> successResult = Result.success(TEST_VALUE);
            Function<String, Integer> throwingTransform = s -> {
                throw new RuntimeException("Transform error");
            };

            // When
            Result<Integer> transformedResult = successResult.map(throwingTransform);

            // Then
            assertThat(transformedResult.isFailure()).isTrue();
            assertThat(transformedResult.exceptionOrNull())
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Transform error");
        }

        @Test
        @DisplayName("Should catch all throwables when mapCatching() transformation throws")
        void should_catchAllThrowables_when_mapCatchingTransformationThrows() {
            // Given
            Result<String> successResult = Result.success(TEST_VALUE);
            Function<String, Integer> throwingTransform = s -> {
                throw new OutOfMemoryError("Serious error");
            };

            // When
            Result<Integer> transformedResult = successResult.mapCatching(throwingTransform);

            // Then
            assertThat(transformedResult.isFailure()).isTrue();
            assertThat(transformedResult.exceptionOrNull())
                .isInstanceOf(OutOfMemoryError.class)
                .hasMessage("Serious error");
        }
    }

    @Nested
    @DisplayName("Monadic Operations")
    class MonadicOperations {

        @Test
        @DisplayName("Should chain successful results when flatMap() called")
        void should_chainSuccessfulResults_when_flatMapCalled() {
            // Given
            Result<String> successResult = Result.success("5");
            Function<String, Result<Integer>> transform = s -> Result.success(Integer.parseInt(s));

            // When
            Result<Integer> chainedResult = successResult.flatMap(transform);

            // Then
            assertThat(chainedResult.isSuccess()).isTrue();
            assertThat(chainedResult.getOrNull()).isEqualTo(5);
        }

        @Test
        @DisplayName("Should preserve first failure when flatMap() called on failed result")
        void should_preserveFirstFailure_when_flatMapCalledOnFailure() {
            // Given
            Result<String> failureResult = Result.failure(TEST_EXCEPTION);
            Function<String, Result<Integer>> transform = s -> Result.success(Integer.parseInt(s));

            // When
            Result<Integer> chainedResult = failureResult.flatMap(transform);

            // Then
            assertThat(chainedResult.isFailure()).isTrue();
            assertThat(chainedResult.exceptionOrNull()).isEqualTo(TEST_EXCEPTION);
        }

        @Test
        @DisplayName("Should return transformation failure when flatMap() transformation returns failure")
        void should_returnTransformationFailure_when_flatMapTransformationFails() {
            // Given
            Result<String> successResult = Result.success("invalid");
            Function<String, Result<Integer>> transform = s -> Result.failure(new NumberFormatException("Not a number"));

            // When
            Result<Integer> chainedResult = successResult.flatMap(transform);

            // Then
            assertThat(chainedResult.isFailure()).isTrue();
            assertThat(chainedResult.exceptionOrNull())
                .isInstanceOf(NumberFormatException.class)
                .hasMessage("Not a number");
        }

        @Test
        @DisplayName("Should catch exceptions when flatMapCatching() transformation throws")
        void should_catchExceptions_when_flatMapCatchingTransformationThrows() {
            // Given
            Result<String> successResult = Result.success("test");
            Function<String, Result<Integer>> throwingTransform = s -> {
                throw new RuntimeException("FlatMap error");
            };

            // When
            Result<Integer> chainedResult = successResult.flatMapCatching(throwingTransform);

            // Then
            assertThat(chainedResult.isFailure()).isTrue();
            assertThat(chainedResult.exceptionOrNull())
                .isInstanceOf(RuntimeException.class)
                .hasMessage("FlatMap error");
        }
    }

    @Nested
    @DisplayName("Recovery Operations")
    class RecoveryOperations {

        @Test
        @DisplayName("Should recover from failure when recover() called")
        void should_recoverFromFailure_when_recoverCalled() {
            // Given
            Result<String> failureResult = Result.failure(TEST_EXCEPTION);
            Function<Throwable, String> recovery = ex -> "Recovered: " + ex.getMessage();

            // When
            Result<String> recoveredResult = failureResult.recover(recovery);

            // Then
            assertThat(recoveredResult.isSuccess()).isTrue();
            assertThat(recoveredResult.getOrNull()).isEqualTo("Recovered: Test exception");
        }

        @Test
        @DisplayName("Should preserve success when recover() called on successful result")
        void should_preserveSuccess_when_recoverCalledOnSuccess() {
            // Given
            Result<String> successResult = Result.success(TEST_VALUE);
            Function<Throwable, String> recovery = ex -> "Recovered: " + ex.getMessage();

            // When
            Result<String> recoveredResult = successResult.recover(recovery);

            // Then
            assertThat(recoveredResult.isSuccess()).isTrue();
            assertThat(recoveredResult.getOrNull()).isEqualTo(TEST_VALUE);
            assertThat(recoveredResult).isSameAs(successResult); // Should return same instance
        }

        @Test
        @DisplayName("Should handle recovery function exceptions when recover() called")
        void should_handleRecoveryFunctionExceptions_when_recoverCalled() {
            // Given
            Result<String> failureResult = Result.failure(TEST_EXCEPTION);
            Function<Throwable, String> throwingRecovery = ex -> {
                throw new IllegalStateException("Recovery failed");
            };

            // When
            Result<String> recoveredResult = failureResult.recover(throwingRecovery);

            // Then
            assertThat(recoveredResult.isFailure()).isTrue();
            assertThat(recoveredResult.exceptionOrNull())
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Recovery failed");
        }

        @Test
        @DisplayName("Should catch all throwables when recoverCatching() recovery throws")
        void should_catchAllThrowables_when_recoverCatchingRecoveryThrows() {
            // Given
            Result<String> failureResult = Result.failure(TEST_EXCEPTION);
            Function<Throwable, String> throwingRecovery = ex -> {
                throw new OutOfMemoryError("Recovery error");
            };

            // When
            Result<String> recoveredResult = failureResult.recoverCatching(throwingRecovery);

            // Then
            assertThat(recoveredResult.isFailure()).isTrue();
            assertThat(recoveredResult.exceptionOrNull())
                .isInstanceOf(OutOfMemoryError.class)
                .hasMessage("Recovery error");
        }
    }

    @Nested
    @DisplayName("Side Effect Operations")
    class SideEffectOperations {

        @Test
        @DisplayName("Should execute action when onSuccess() called on successful result")
        void should_executeAction_when_onSuccessCalledOnSuccess() {
            // Given
            Result<String> successResult = Result.success(TEST_VALUE);
            AtomicBoolean actionExecuted = new AtomicBoolean(false);

            // When
            Result<String> result = successResult.onSuccess(value -> actionExecuted.set(true));

            // Then
            assertThat(actionExecuted.get()).isTrue();
            assertThat(result).isSameAs(successResult); // Should return same instance
        }

        @Test
        @DisplayName("Should not execute action when onSuccess() called on failed result")
        void should_notExecuteAction_when_onSuccessCalledOnFailure() {
            // Given
            Result<String> failureResult = Result.failure(TEST_EXCEPTION);
            AtomicBoolean actionExecuted = new AtomicBoolean(false);

            // When
            Result<String> result = failureResult.onSuccess(value -> actionExecuted.set(true));

            // Then
            assertThat(actionExecuted.get()).isFalse();
            assertThat(result).isSameAs(failureResult); // Should return same instance
        }

        @Test
        @DisplayName("Should execute action when onFailure() called on failed result")
        void should_executeAction_when_onFailureCalledOnFailure() {
            // Given
            Result<String> failureResult = Result.failure(TEST_EXCEPTION);
            AtomicBoolean actionExecuted = new AtomicBoolean(false);

            // When
            Result<String> result = failureResult.onFailure(ex -> actionExecuted.set(true));

            // Then
            assertThat(actionExecuted.get()).isTrue();
            assertThat(result).isSameAs(failureResult); // Should return same instance
        }

        @Test
        @DisplayName("Should not execute action when onFailure() called on successful result")
        void should_notExecuteAction_when_onFailureCalledOnSuccess() {
            // Given
            Result<String> successResult = Result.success(TEST_VALUE);
            AtomicBoolean actionExecuted = new AtomicBoolean(false);

            // When
            Result<String> result = successResult.onFailure(ex -> actionExecuted.set(true));

            // Then
            assertThat(actionExecuted.get()).isFalse();
            assertThat(result).isSameAs(successResult); // Should return same instance
        }

        @Test
        @DisplayName("Should execute peek action and return original result")
        void should_executePeekAction_when_peekCalledOnSuccess() {
            // Given
            Result<String> successResult = Result.success(TEST_VALUE);
            AtomicBoolean actionExecuted = new AtomicBoolean(false);

            // When
            Result<String> result = successResult.peek(value -> actionExecuted.set(true));

            // Then
            assertThat(actionExecuted.get()).isTrue();
            assertThat(result).isSameAs(successResult);
        }

        @Test
        @DisplayName("Should execute peekError action on failure")
        void should_executePeekErrorAction_when_peekErrorCalledOnFailure() {
            // Given
            Result<String> failureResult = Result.failure(TEST_EXCEPTION);
            AtomicBoolean actionExecuted = new AtomicBoolean(false);

            // When
            Result<String> result = failureResult.peekError(ex -> actionExecuted.set(true));

            // Then
            assertThat(actionExecuted.get()).isTrue();
            assertThat(result).isSameAs(failureResult);
        }
    }

    @Nested
    @DisplayName("Fold Operations")
    class FoldOperations {

        @Test
        @DisplayName("Should apply success function when fold() called on successful result")
        void should_applySuccessFunction_when_foldCalledOnSuccess() {
            // Given
            Result<String> successResult = Result.success(TEST_VALUE);
            Function<String, Integer> onSuccess = String::length;
            Function<Throwable, Integer> onFailure = ex -> -1;

            // When
            Integer result = successResult.fold(onSuccess, onFailure);

            // Then
            assertThat(result).isEqualTo(TEST_VALUE.length());
        }

        @Test
        @DisplayName("Should apply failure function when fold() called on failed result")
        void should_applyFailureFunction_when_foldCalledOnFailure() {
            // Given
            Result<String> failureResult = Result.failure(TEST_EXCEPTION);
            Function<String, Integer> onSuccess = String::length;
            Function<Throwable, Integer> onFailure = ex -> -1;

            // When
            Integer result = failureResult.fold(onSuccess, onFailure);

            // Then
            assertThat(result).isEqualTo(-1);
        }
    }

    @Nested
    @DisplayName("Filtering Operations")
    class FilteringOperations {

        @Test
        @DisplayName("Should preserve success when filter() predicate passes")
        void should_preserveSuccess_when_filterPredicatePasses() {
            // Given
            Result<String> successResult = Result.success(TEST_VALUE);
            Supplier<IllegalArgumentException> exceptionSupplier = () -> ILLEGAL_ARG_EXCEPTION;

            // When
            Result<String> filteredResult = successResult.filter(s -> s.equals(TEST_VALUE), exceptionSupplier);

            // Then
            assertThat(filteredResult.isSuccess()).isTrue();
            assertThat(filteredResult.getOrNull()).isEqualTo(TEST_VALUE);
        }

        @Test
        @DisplayName("Should create failure when filter() predicate fails")
        void should_createFailure_when_filterPredicateFails() {
            // Given
            Result<String> successResult = Result.success(TEST_VALUE);
            Supplier<IllegalArgumentException> exceptionSupplier = () -> ILLEGAL_ARG_EXCEPTION;

            // When
            Result<String> filteredResult = successResult.filter(s -> s.equals("different"), exceptionSupplier);

            // Then
            assertThat(filteredResult.isFailure()).isTrue();
            assertThat(filteredResult.exceptionOrNull()).isEqualTo(ILLEGAL_ARG_EXCEPTION);
        }

        @Test
        @DisplayName("Should preserve failure when filter() called on failed result")
        void should_preserveFailure_when_filterCalledOnFailure() {
            // Given
            Result<String> failureResult = Result.failure(TEST_EXCEPTION);
            Supplier<IllegalArgumentException> exceptionSupplier = () -> ILLEGAL_ARG_EXCEPTION;

            // When
            Result<String> filteredResult = failureResult.filter(s -> true, exceptionSupplier);

            // Then
            assertThat(filteredResult.isFailure()).isTrue();
            assertThat(filteredResult.exceptionOrNull()).isEqualTo(TEST_EXCEPTION);
        }
    }

    @Nested
    @DisplayName("Conversion Operations")
    class ConversionOperations {

        @Test
        @DisplayName("Should convert to Optional.of() when toOptional() called on successful result")
        void should_convertToOptionalOf_when_toOptionalCalledOnSuccess() {
            // Given
            Result<String> successResult = Result.success(TEST_VALUE);

            // When
            Optional<String> optional = successResult.toOptional();

            // Then
            assertThat(optional).isPresent().contains(TEST_VALUE);
        }

        @Test
        @DisplayName("Should convert to Optional.empty() when toOptional() called on failed result")
        void should_convertToOptionalEmpty_when_toOptionalCalledOnFailure() {
            // Given
            Result<String> failureResult = Result.failure(TEST_EXCEPTION);

            // When
            Optional<String> optional = failureResult.toOptional();

            // Then
            assertThat(optional).isEmpty();
        }

        @Test
        @DisplayName("Should convert to Stream with value when stream() called on successful result")
        void should_convertToStreamWithValue_when_streamCalledOnSuccess() {
            // Given
            Result<String> successResult = Result.success(TEST_VALUE);

            // When
            List<String> streamResult = successResult.stream().collect(Collectors.toList());

            // Then
            assertThat(streamResult).containsExactly(TEST_VALUE);
        }

        @Test
        @DisplayName("Should convert to empty Stream when stream() called on failed result")
        void should_convertToEmptyStream_when_streamCalledOnFailure() {
            // Given
            Result<String> failureResult = Result.failure(TEST_EXCEPTION);

            // When
            List<String> streamResult = failureResult.stream().collect(Collectors.toList());

            // Then
            assertThat(streamResult).isEmpty();
        }
    }

    @Nested
    @DisplayName("Combination Operations")
    class CombinationOperations {

        @Test
        @DisplayName("Should combine two successful results")
        void should_combineTwoSuccessfulResults_when_combineCalledOnSuccess() {
            // Given
            Result<String> result1 = Result.success("Hello");
            Result<String> result2 = Result.success("World");

            // When
            Result<String> combinedResult = result1.combine(result2, (s1, s2) -> s1 + " " + s2);

            // Then
            assertThat(combinedResult.isSuccess()).isTrue();
            assertThat(combinedResult.getOrNull()).isEqualTo("Hello World");
        }

        @Test
        @DisplayName("Should return first failure when combine() called with failed first result")
        void should_returnFirstFailure_when_combineCalledWithFailedFirstResult() {
            // Given
            Result<String> result1 = Result.failure(TEST_EXCEPTION);
            Result<String> result2 = Result.success("World");

            // When
            Result<String> combinedResult = result1.combine(result2, (s1, s2) -> s1 + " " + s2);

            // Then
            assertThat(combinedResult.isFailure()).isTrue();
            assertThat(combinedResult.exceptionOrNull()).isEqualTo(TEST_EXCEPTION);
        }

        @Test
        @DisplayName("Should return second failure when combine() called with failed second result")
        void should_returnSecondFailure_when_combineCalledWithFailedSecondResult() {
            // Given
            Result<String> result1 = Result.success("Hello");
            Result<String> result2 = Result.failure(ILLEGAL_ARG_EXCEPTION);

            // When
            Result<String> combinedResult = result1.combine(result2, (s1, s2) -> s1 + " " + s2);

            // Then
            assertThat(combinedResult.isFailure()).isTrue();
            assertThat(combinedResult.exceptionOrNull()).isEqualTo(ILLEGAL_ARG_EXCEPTION);
        }

        @Test
        @DisplayName("Should handle combiner function exceptions")
        void should_handleCombinerFunctionExceptions_when_combineCalledWithThrowingCombiner() {
            // Given
            Result<String> result1 = Result.success("Hello");
            Result<String> result2 = Result.success("World");

            // When
            Result<String> combinedResult = result1.combine(result2, (s1, s2) -> {
                throw new RuntimeException("Combiner error");
            });

            // Then
            assertThat(combinedResult.isFailure()).isTrue();
            assertThat(combinedResult.exceptionOrNull())
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Combiner error");
        }
    }

    @Nested
    @DisplayName("Static Factory Methods")
    class StaticFactoryMethods {

        @Test
        @DisplayName("Should create success when fromPredicate() predicate passes")
        void should_createSuccess_when_fromPredicatePredicatePasses() {
            // Given
            String value = TEST_VALUE;
            Supplier<IllegalArgumentException> exceptionSupplier = () -> ILLEGAL_ARG_EXCEPTION;

            // When
            Result<String> result = Result.fromPredicate(value, s -> s.length() > 5, exceptionSupplier);

            // Then
            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getOrNull()).isEqualTo(value);
        }

        @Test
        @DisplayName("Should create failure when fromPredicate() predicate fails")
        void should_createFailure_when_fromPredicatePredicateFails() {
            // Given
            String value = "hi";
            Supplier<IllegalArgumentException> exceptionSupplier = () -> ILLEGAL_ARG_EXCEPTION;

            // When
            Result<String> result = Result.fromPredicate(value, s -> s.length() > 5, exceptionSupplier);

            // Then
            assertThat(result.isFailure()).isTrue();
            assertThat(result.exceptionOrNull()).isEqualTo(ILLEGAL_ARG_EXCEPTION);
        }

        @Test
        @DisplayName("Should create success when fromNullable() called with non-null value")
        void should_createSuccess_when_fromNullableCalledWithNonNullValue() {
            // Given
            String value = TEST_VALUE;

            // When
            Result<String> result = Result.fromNullable(value);

            // Then
            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getOrNull()).isEqualTo(value);
        }

        @Test
        @DisplayName("Should create failure when fromNullable() called with null value")
        void should_createFailure_when_fromNullableCalledWithNullValue() {
            // Given
            String nullValue = null;

            // When
            Result<String> result = Result.fromNullable(nullValue);

            // Then
            assertThat(result.isFailure()).isTrue();
            assertThat(result.exceptionOrNull())
                .isInstanceOf(NullPointerException.class)
                .hasMessage("Value is null");
        }

        @Test
        @DisplayName("Should create failure with custom exception when fromNullable() called with null and custom supplier")
        void should_createFailureWithCustomException_when_fromNullableCalledWithNullAndCustomSupplier() {
            // Given
            String nullValue = null;
            Supplier<IllegalArgumentException> exceptionSupplier = () -> ILLEGAL_ARG_EXCEPTION;

            // When
            Result<String> result = Result.fromNullable(nullValue, exceptionSupplier);

            // Then
            assertThat(result.isFailure()).isTrue();
            assertThat(result.exceptionOrNull()).isEqualTo(ILLEGAL_ARG_EXCEPTION);
        }

        @Test
        @DisplayName("Should create success when fromSupplier() supplier succeeds")
        void should_createSuccess_when_fromSupplierSupplierSucceeds() {
            // Given
            Supplier<String> supplier = () -> TEST_VALUE;

            // When
            Result<String> result = Result.fromSupplier(supplier);

            // Then
            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getOrNull()).isEqualTo(TEST_VALUE);
        }

        @Test
        @DisplayName("Should create failure when fromSupplier() supplier throws")
        void should_createFailure_when_fromSupplierSupplierThrows() {
            // Given
            Supplier<String> throwingSupplier = () -> {
                throw TEST_EXCEPTION;
            };

            // When
            Result<String> result = Result.fromSupplier(throwingSupplier);

            // Then
            assertThat(result.isFailure()).isTrue();
            assertThat(result.exceptionOrNull()).isEqualTo(TEST_EXCEPTION);
        }

        @Test
        @DisplayName("Should create success when runCatching() supplier succeeds")
        void should_createSuccess_when_runCatchingSupplierSucceeds() {
            // Given
            Supplier<String> supplier = () -> TEST_VALUE;

            // When
            Result<String> result = Result.runCatching(supplier);

            // Then
            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getOrNull()).isEqualTo(TEST_VALUE);
        }

        @Test
        @DisplayName("Should create failure when runCatching() supplier throws RuntimeException")
        void should_createFailure_when_runCatchingSupplierThrowsRuntimeException() {
            // Given
            Supplier<String> throwingSupplier = () -> {
                throw TEST_EXCEPTION;
            };

            // When
            Result<String> result = Result.runCatching(throwingSupplier);

            // Then
            assertThat(result.isFailure()).isTrue();
            assertThat(result.exceptionOrNull()).isEqualTo(TEST_EXCEPTION);
        }

        @Test
        @DisplayName("Should create failure when runCatching() supplier throws checked Exception")
        void should_createFailure_when_runCatchingSupplierThrowsCheckedException() {
            // Given
            Supplier<String> throwingSupplier = () -> {
                try {
                    throw new IOException("IO error");
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            };

            // When
            Result<String> result = Result.runCatching(throwingSupplier);

            // Then
            assertThat(result.isFailure()).isTrue();
            assertThat(result.exceptionOrNull()).isInstanceOf(RuntimeException.class);
            assertThat(result.exceptionOrNull().getCause()).isInstanceOf(IOException.class);
            assertThat(result.exceptionOrNull().getCause().getMessage()).isEqualTo("IO error");
        }

        @Test
        @DisplayName("Should create success when runCatching() returns null")
        void should_createSuccess_when_runCatchingReturnsNull() {
            // Given
            Supplier<String> nullSupplier = () -> null;

            // When
            Result<String> result = Result.runCatching(nullSupplier);

            // Then
            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getOrNull()).isNull();
        }

        @Test
        @DisplayName("Should create success when runCatching() with complex computation")
        void should_createSuccess_when_runCatchingWithComplexComputation() {
            // Given
            Supplier<Integer> complexSupplier = () -> {
                int result = 0;
                for (int i = 1; i <= 10; i++) {
                    result += i * i;
                }
                return result;
            };

            // When
            Result<Integer> result = Result.runCatching(complexSupplier);

            // Then
            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getOrNull()).isEqualTo(385); // Sum of squares from 1 to 10
        }

        @Test
        @DisplayName("Should handle Error subclasses when runCatching() supplier throws Error")
        void should_handleErrorSubclasses_when_runCatchingSupplierThrowsError() {
            // Given
            OutOfMemoryError error = new OutOfMemoryError("Out of memory");
            Supplier<String> errorThrowingSupplier = () -> {
                throw error;
            };

            // When
            Result<String> result = Result.runCatching(errorThrowingSupplier);

            // Then
            assertThat(result.isFailure()).isTrue();
            assertThat(result.exceptionOrNull()).isEqualTo(error);
        }

        @Test
        @DisplayName("Should work with method references in runCatching()")
        void should_workWithMethodReferences_when_usingRunCatching() {
            // Given
            String input = "  hello world  ";

            // When
            Result<String> result = Result.runCatching(input::trim);

            // Then
            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getOrNull()).isEqualTo("hello world");
        }

        @Test
        @DisplayName("Should chain runCatching() with other Result operations")
        void should_chainRunCatching_withOtherResultOperations() {
            // Given
            Supplier<String> supplier = () -> "42";

            // When
            Result<Integer> result = Result.runCatching(supplier)
                    .map(Integer::parseInt)
                    .map(i -> i * 2);

            // Then
            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getOrNull()).isEqualTo(84);
        }

        @Test
        @DisplayName("Should handle exception in chained operations after runCatching()")
        void should_handleExceptionInChainedOperations_afterRunCatching() {
            // Given
            Supplier<String> supplier = () -> "not-a-number";

            // When
            Result<Integer> result = Result.runCatching(supplier)
                    .map(Integer::parseInt);

            // Then
            assertThat(result.isFailure()).isTrue();
            assertThat(result.exceptionOrNull()).isInstanceOf(NumberFormatException.class);
        }

        @Test
        @DisplayName("Should create success when fromCondition() condition is true")
        void should_createSuccess_when_fromConditionConditionIsTrue() {
            // Given
            boolean condition = true;
            String value = TEST_VALUE;
            Supplier<IllegalArgumentException> exceptionSupplier = () -> ILLEGAL_ARG_EXCEPTION;

            // When
            Result<String> result = Result.fromCondition(condition, value, exceptionSupplier);

            // Then
            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getOrNull()).isEqualTo(value);
        }

        @Test
        @DisplayName("Should create failure when fromCondition() condition is false")
        void should_createFailure_when_fromConditionConditionIsFalse() {
            // Given
            boolean condition = false;
            String value = TEST_VALUE;
            Supplier<IllegalArgumentException> exceptionSupplier = () -> ILLEGAL_ARG_EXCEPTION;

            // When
            Result<String> result = Result.fromCondition(condition, value, exceptionSupplier);

            // Then
            assertThat(result.isFailure()).isTrue();
            assertThat(result.exceptionOrNull()).isEqualTo(ILLEGAL_ARG_EXCEPTION);
        }

        @Test
        @DisplayName("Should create success when fromOptional() called with present Optional")
        void should_createSuccess_when_fromOptionalCalledWithPresentOptional() {
            // Given
            Optional<String> optional = Optional.of(TEST_VALUE);
            Supplier<IllegalArgumentException> exceptionSupplier = () -> ILLEGAL_ARG_EXCEPTION;

            // When
            Result<String> result = Result.fromOptional(optional, exceptionSupplier);

            // Then
            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getOrNull()).isEqualTo(TEST_VALUE);
        }

        @Test
        @DisplayName("Should create failure when fromOptional() called with empty Optional")
        void should_createFailure_when_fromOptionalCalledWithEmptyOptional() {
            // Given
            Optional<String> optional = Optional.empty();
            Supplier<IllegalArgumentException> exceptionSupplier = () -> ILLEGAL_ARG_EXCEPTION;

            // When
            Result<String> result = Result.fromOptional(optional, exceptionSupplier);

            // Then
            assertThat(result.isFailure()).isTrue();
            assertThat(result.exceptionOrNull()).isEqualTo(ILLEGAL_ARG_EXCEPTION);
        }
    }

    @Nested
    @DisplayName("Sequence Operations")
    class SequenceOperations {

        @Test
        @DisplayName("Should sequence all successful results into a list")
        void should_sequenceAllSuccessfulResults_when_sequenceCalledWithAllSuccesses() {
            // Given
            Result<String> result1 = Result.success("A");
            Result<String> result2 = Result.success("B");
            Result<String> result3 = Result.success("C");

            // When
            Result<List<String>> sequencedResult = Result.sequence(result1, result2, result3);

            // Then
            assertThat(sequencedResult.isSuccess()).isTrue();
            assertThat(sequencedResult.getOrNull()).containsExactly("A", "B", "C");
        }

        @Test
        @DisplayName("Should return first failure when sequence() called with mixed results")
        void should_returnFirstFailure_when_sequenceCalledWithMixedResults() {
            // Given
            Result<String> result1 = Result.success("A");
            Result<String> result2 = Result.failure(TEST_EXCEPTION);
            Result<String> result3 = Result.success("C");

            // When
            Result<List<String>> sequencedResult = Result.sequence(result1, result2, result3);

            // Then
            assertThat(sequencedResult.isFailure()).isTrue();
            assertThat(sequencedResult.exceptionOrNull()).isEqualTo(TEST_EXCEPTION);
        }

        @Test
        @DisplayName("Should handle empty sequence")
        void should_handleEmptySequence_when_sequenceCalledWithNoResults() {
            // When
            Result<List<String>> sequencedResult = Result.sequence();

            // Then
            assertThat(sequencedResult.isSuccess()).isTrue();
            assertThat(sequencedResult.getOrNull()).isEmpty();
        }
    }

    @Nested
    @DisplayName("Parameterized Tests")
    class ParameterizedTests {

        @ParameterizedTest(name = "Should handle getOrElse with supplier for value: {0}")
        @ValueSource(strings = {"test1", "test2", "test3"})
        @DisplayName("Should return value when getOrElse with supplier called on successful results")
        void should_returnValue_when_getOrElseWithSupplierCalledOnSuccess(String value) {
            // Given
            Result<String> successResult = Result.success(value);
            Supplier<String> supplier = () -> "default";

            // When
            String result = successResult.getOrElse(supplier);

            // Then
            assertThat(result).isEqualTo(value);
        }

        @ParameterizedTest(name = "Should handle different exception types: {0}")
        @CsvSource({
            "RuntimeException, Runtime error message",
            "IllegalArgumentException, Illegal argument message",
            "IllegalStateException, Illegal state message"
        })
        @DisplayName("Should handle different exception types correctly")
        void should_handleDifferentExceptionTypes_when_creatingFailureResults(String exceptionType, String message) {
            // Given
            Throwable exception = switch (exceptionType) {
                case "RuntimeException" -> new RuntimeException(message);
                case "IllegalArgumentException" -> new IllegalArgumentException(message);
                case "IllegalStateException" -> new IllegalStateException(message);
                default -> throw new IllegalArgumentException("Unknown exception type: " + exceptionType);
            };

            // When
            Result<String> result = Result.failure(exception);

            // Then
            assertThat(result.isFailure()).isTrue();
            assertThat(result.exceptionOrNull()).isEqualTo(exception);
            assertThat(result.exceptionOrNull().getMessage()).isEqualTo(message);
        }
    }

    @Nested
    @DisplayName("Object Contract Tests")
    class ObjectContractTests {

        @Test
        @DisplayName("Should implement toString() correctly for successful result")
        void should_implementToStringCorrectly_when_calledOnSuccessfulResult() {
            // Given
            Result<String> successResult = Result.success(TEST_VALUE);

            // When
            String stringRepresentation = successResult.toString();

            // Then
            assertThat(stringRepresentation).isEqualTo("Success(" + TEST_VALUE + ")");
        }

        @Test
        @DisplayName("Should implement toString() correctly for failed result")
        void should_implementToStringCorrectly_when_calledOnFailedResult() {
            // Given
            Result<String> failureResult = Result.failure(TEST_EXCEPTION);

            // When
            String stringRepresentation = failureResult.toString();

            // Then
            assertThat(stringRepresentation).isEqualTo("Failure(" + TEST_EXCEPTION + ")");
        }

        @Test
        @DisplayName("Should implement equals() correctly for successful results")
        void should_implementEqualsCorrectly_when_comparingSuccessfulResults() {
            // Given
            Result<String> result1 = Result.success(TEST_VALUE);
            Result<String> result2 = Result.success(TEST_VALUE);
            Result<String> result3 = Result.success(ANOTHER_VALUE);

            // When & Then
            assertThat(result1).isEqualTo(result2);
            assertThat(result1).isNotEqualTo(result3);
            assertThat(result1).isNotEqualTo(null);
            assertThat(result1).isEqualTo(result1); // reflexive
        }

        @Test
        @DisplayName("Should implement equals() correctly for failed results")
        void should_implementEqualsCorrectly_when_comparingFailedResults() {
            // Given
            Result<String> result1 = Result.failure(TEST_EXCEPTION);
            Result<String> result2 = Result.failure(TEST_EXCEPTION);
            Result<String> result3 = Result.failure(ILLEGAL_ARG_EXCEPTION);

            // When & Then
            assertThat(result1).isEqualTo(result2);
            assertThat(result1).isNotEqualTo(result3);
        }

        @Test
        @DisplayName("Should implement hashCode() correctly")
        void should_implementHashCodeCorrectly_when_called() {
            // Given
            Result<String> result1 = Result.success(TEST_VALUE);
            Result<String> result2 = Result.success(TEST_VALUE);
            Result<String> result3 = Result.failure(TEST_EXCEPTION);

            // When & Then
            assertThat(result1.hashCode()).isEqualTo(result2.hashCode()); // Equal objects have equal hash codes
            assertThat(result1.hashCode()).isNotEqualTo(result3.hashCode()); // Different objects should have different hash codes
        }

        @Test
        @DisplayName("Should not equal different types")
        void should_notEqualDifferentTypes_when_equalsCalledWithDifferentType() {
            // Given
            Result<String> result = Result.success(TEST_VALUE);
            String differentType = "different";

            // When & Then
            assertThat(result).isNotEqualTo(differentType);
        }
    }

    @Nested
    @DisplayName("Serialization Tests")
    class SerializationTests {

        @Test
        @DisplayName("Should serialize and deserialize successful result correctly")
        void should_serializeAndDeserializeSuccessfulResult_when_calledCorrectly() throws IOException, ClassNotFoundException {
            // Given
            Result<String> originalResult = Result.success(TEST_VALUE);

            // When
            byte[] serializedData = serialize(originalResult);
            Result<String> deserializedResult = deserialize(serializedData);

            // Then
            assertThat(deserializedResult).isEqualTo(originalResult);
            assertThat(deserializedResult.isSuccess()).isTrue();
            assertThat(deserializedResult.getOrNull()).isEqualTo(TEST_VALUE);
        }

        @Test
        @DisplayName("Should serialize and deserialize failed result correctly")
        void should_serializeAndDeserializeFailedResult_when_calledCorrectly() throws IOException, ClassNotFoundException {
            // Given
            Result<String> originalResult = Result.failure(TEST_EXCEPTION);

            // When
            byte[] serializedData = serialize(originalResult);
            Result<String> deserializedResult = deserialize(serializedData);

            // Then
            // Compare the properties individually since object identity may differ after deserialization
            assertThat(deserializedResult.isFailure()).isTrue();
            assertThat(deserializedResult.isSuccess()).isEqualTo(originalResult.isSuccess());
            assertThat(deserializedResult.exceptionOrNull()).isInstanceOf(RuntimeException.class);
            assertThat(deserializedResult.exceptionOrNull().getMessage()).isEqualTo(TEST_EXCEPTION.getMessage());
            assertThat(deserializedResult.getOrNull()).isEqualTo(originalResult.getOrNull());
        }

        private byte[] serialize(Result<String> result) throws IOException {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
                oos.writeObject(result);
            }
            return baos.toByteArray();
        }

        @SuppressWarnings("unchecked")
        private Result<String> deserialize(byte[] data) throws IOException, ClassNotFoundException {
            ByteArrayInputStream bais = new ByteArrayInputStream(data);
            try (ObjectInputStream ois = new ObjectInputStream(bais)) {
                return (Result<String>) ois.readObject();
            }
        }
    }

    @Nested
    @DisplayName("Edge Cases and Boundary Conditions")
    class EdgeCasesAndBoundaryConditions {

        @Test
        @DisplayName("Should handle null values in successful results")
        void should_handleNullValues_when_successfulResultContainsNull() {
            // Given
            Result<String> nullSuccessResult = Result.success(null);

            // When & Then
            assertThat(nullSuccessResult.isSuccess()).isTrue();
            assertThat(nullSuccessResult.getOrNull()).isNull();
            // getOrDefault returns the value (null) when successful, even if value is null
            assertThat(nullSuccessResult.getOrDefault("default")).isNull();
        }

        @Test
        @DisplayName("Should handle chaining operations on null value success")
        void should_handleChainingOperations_when_successfulResultContainsNull() {
            // Given
            Result<String> nullSuccessResult = Result.success(null);

            // When
            Result<Integer> mappedResult = nullSuccessResult.map(s -> s == null ? 0 : s.length());

            // Then
            assertThat(mappedResult.isSuccess()).isTrue();
            assertThat(mappedResult.getOrNull()).isEqualTo(0);
        }

        @Test
        @DisplayName("Should handle multiple consecutive transformations")
        void should_handleMultipleConsecutiveTransformations_when_chained() {
            // Given
            Result<String> initialResult = Result.success("hello");

            // When
            Result<String> finalResult = initialResult
                .map(String::toUpperCase)
                .flatMap(s -> Result.success(s + " WORLD"))
                .filter(s -> s.length() > 5, () -> new IllegalArgumentException("Too short"))
                .recover(ex -> "RECOVERED");

            // Then
            assertThat(finalResult.isSuccess()).isTrue();
            assertThat(finalResult.getOrNull()).isEqualTo("HELLO WORLD");
        }

        @Test
        @DisplayName("Should handle transformation chain with failure in middle")
        void should_handleTransformationChainWithFailure_when_failureOccursInMiddle() {
            // Given
            Result<String> initialResult = Result.success("hello");

            // When
            Result<String> finalResult = initialResult
                .map(String::toUpperCase)
                .flatMap(s -> Result.failure(new RuntimeException("Middle failure")))
                .map(s -> s + " WORLD") // This should not execute
                .recover(ex -> "RECOVERED");

            // Then
            assertThat(finalResult.isSuccess()).isTrue();
            assertThat(finalResult.getOrNull()).isEqualTo("RECOVERED");
        }

        @Test
        @DisplayName("Should handle concurrent access to Result instances")
        void should_handleConcurrentAccess_when_accessedFromMultipleThreads() throws InterruptedException {
            // Given
            Result<String> result = Result.success(TEST_VALUE);
            AtomicInteger successCount = new AtomicInteger(0);
            int threadCount = 10;
            Thread[] threads = new Thread[threadCount];

            // When
            for (int i = 0; i < threadCount; i++) {
                threads[i] = new Thread(() -> {
                    result.onSuccess(value -> successCount.incrementAndGet());
                });
                threads[i].start();
            }

            for (Thread thread : threads) {
                thread.join();
            }

            // Then
            assertThat(successCount.get()).isEqualTo(threadCount);
        }
    }
}
