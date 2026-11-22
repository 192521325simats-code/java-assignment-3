package q7_concurrent_result_processor;

/**
 * Diagnostic audit object capturing a processing failure without halting the system.
 */
public record ProcessingFailure(String fileName, String failureType, String errorMessage) {
}
