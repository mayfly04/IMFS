package com.mayfly.imfs.validator;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import com.mayfly.imfs.constants.EntityType;
import com.mayfly.imfs.constants.FileSystemConstants;
import com.mayfly.imfs.exception.NameConflictException;

class FSValidatorTest {

    @ParameterizedTest
    @CsvSource({
        "FOLDER,invalid*name," + FileSystemConstants.ENTITY_NAME_VALIDATION_EXCP,
        "TEXT_FILE,bad@file.txt," + FileSystemConstants.FILE_NAME_VALIDATION_EXCP
    })
    void validateEntityName_invalid_throwsException(EntityType type, String name, String expectedMsg) {
        NameConflictException ex = assertThrows(NameConflictException.class, () ->
            FSValidator.validateEntityName(type, name)
        );
        assertTrue(ex.getMessage().contains(expectedMsg));
    }

    @ParameterizedTest
    @CsvSource({
        "FOLDER,Folder123",
        "TEXT_FILE,file1.txt"
    })
    void validateEntityName_valid_noException(EntityType type, String name) {
        assertDoesNotThrow(() -> FSValidator.validateEntityName(type, name));
    }

    @ParameterizedTest
    @MethodSource("invalidFileNames")
    void validateFileName_invalid_throwsException(String fileName) {
        assertThrows(NameConflictException.class, () -> FSValidator.validateFileName(fileName));
    }

    static Stream<String> invalidFileNames() {
        return Stream.of("abc..txt", "a b.txt", "abc.txt.exe");
    }

    @ParameterizedTest
    @ValueSource(strings = {"abc.txt", "abc"})
    void validateFileName_valid_noException(String fileName) {
        assertDoesNotThrow(() -> FSValidator.validateFileName(fileName));
    }

    @ParameterizedTest
    @ValueSource(strings = {"abc123"})
    void validateEntityName_regex_valid(String entityName) {
        assertDoesNotThrow(() -> FSValidator.validateEntityName(entityName));
    }

    @ParameterizedTest
    @ValueSource(strings = {"abc-123", "abc 123"})
    void validateEntityName_regex_invalid(String entityName) {
        assertThrows(NameConflictException.class, () -> FSValidator.validateEntityName(entityName));
    }
}
