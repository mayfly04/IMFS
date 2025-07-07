package com.mayfly.imfs.worker;

import static org.junit.jupiter.api.Assertions.*;

import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.mayfly.imfs.constants.EntityType;
import com.mayfly.imfs.exception.EntityNotFoundException;
import com.mayfly.imfs.exception.InvalidOperationException;
import com.mayfly.imfs.exception.NameConflictException;
import com.mayfly.imfs.model.Entity;
import com.mayfly.imfs.model.FileSystemEntity;
import com.mayfly.imfs.model.TextFile;
import com.mayfly.imfs.service.FileSystemWorker;
import com.mayfly.imfs.utils.FSUtils;

class FileSystemWorkerTest {

    private FileSystemWorker worker;

    @BeforeEach
    void setUp() {
        worker = new FileSystemWorker();
    }

    static Stream<EntityType> fileSystemTypes() {
        return Stream.of(EntityType.FOLDER, EntityType.ZIP_FILE);
    }

    @ParameterizedTest
    @MethodSource("fileSystemTypes")
    void createFileSystem_success(EntityType type) {
        worker.create(EntityType.DRIVE, "D", null);
        worker.create(type, "photos2025", "D");
        
        assertFalse(worker.getDrives().isEmpty());
        assertTrue(worker.getDrives().containsKey("D"));
        FileSystemEntity fs = worker.getDrives().get("D");
        assertNotNull(fs);
        assertNotNull(fs.getChildren());
        Entity childFs = fs.getChild("photos2025");
        assertNotNull(childFs);
        assertTrue(childFs.getName().equals("photos2025"));
        assertTrue(childFs.getType().equals(type));
        
        
        worker.create(EntityType.DRIVE, "AB1", null);
        worker.create(type, "photos2025", "AB1");
        worker.create(EntityType.TEXT_FILE, "txt.file", "AB1");
        
        worker.create(EntityType.TEXT_FILE, "txt2.file", "AB1\\photos2025");
        assertFalse(worker.getDrives().isEmpty());
        assertTrue(worker.getDrives().containsKey("AB1"));
        fs = worker.getDrives().get("AB1");
        assertNotNull(fs);
        assertNotNull(fs.getChildren());
        assertNotNull(fs.getChild("txt.file"));
        childFs = fs.getChild("photos2025");
        assertNotNull(childFs.getLastModified());
        assertEquals("AB1\\photos2025", childFs.getPath());
        FileSystemEntity child = (FileSystemEntity) childFs;
        assertNotNull(child.getChild("txt2.file"));
        assertNotNull(childFs.getLastModified());
        assertEquals("AB1\\photos2025\\txt2.file", child.getChild("txt2.file").getPath());
        assertNotNull(childFs);
        assertTrue(childFs.getName().equals("photos2025"));
        assertTrue(childFs.getType().equals(type));
        
        assertEquals(2, worker.getDrives().size());
    }

    static Stream<EntityType> allTypes() {
        return Stream.of(EntityType.FOLDER, EntityType.ZIP_FILE, EntityType.TEXT_FILE);
    }

    @ParameterizedTest
    @MethodSource("allTypes")
    void createWithInvalidParent_throws(EntityType type) {
        worker.create(EntityType.DRIVE, "E", null);
        worker.create(EntityType.TEXT_FILE, "notes.txt", "E");
        EntityNotFoundException ex = assertThrows(EntityNotFoundException.class, () ->
                worker.create(type, "subfolder", "E/notes.txt"));
        assertTrue(ex.getMessage().contains("Drive not found"));
    }

    @Test
    void createDrive_conflict() {
        worker.create(EntityType.DRIVE, "F", null);
        NameConflictException ex = assertThrows(NameConflictException.class, () ->
                worker.create(EntityType.DRIVE, "F", null));
        assertEquals("Drive already exists: F", ex.getMessage());
    }

    @Test
    void createWithUnknownType_throws() {
        worker.create(EntityType.DRIVE, "G", null);
        InvalidOperationException ex = assertThrows(InvalidOperationException.class, () ->
                worker.create(null, "mystery", "G"));
        assertEquals("Unknown entity type", ex.getMessage());
    }

    @Test
    void deleteDrive_success() {
        worker.create(EntityType.DRIVE, "H", null);
        worker.delete("H");
        worker.create(EntityType.DRIVE, "H", null);
    }

    @Test
    void moveDrive_throws() {
        worker.create(EntityType.DRIVE, "I", null);
        InvalidOperationException ex = assertThrows(InvalidOperationException.class, () ->
                worker.move("I", "I"));
        assertEquals("Cannot move drive", ex.getMessage());
    }
    
    static Stream<Arguments> validMoveEntityCases() {
        // Exclude DRIVE as source or destination except as root
        return Stream.of(
            Arguments.of(EntityType.FOLDER, EntityType.FOLDER),
            Arguments.of(EntityType.FOLDER, EntityType.ZIP_FILE),
            Arguments.of(EntityType.ZIP_FILE, EntityType.FOLDER),
            Arguments.of(EntityType.ZIP_FILE, EntityType.ZIP_FILE)
        );
    }

    @ParameterizedTest
    @MethodSource("validMoveEntityCases")
    void testMoveEntity_validCombinations(EntityType srcType, EntityType destType) {
        FileSystemWorker worker = new FileSystemWorker();
        worker.create(EntityType.DRIVE, "Q", null);
        worker.create(destType, "dest", "Q");
        worker.create(EntityType.DRIVE, "EXT", null);
        worker.create(srcType, "src", "Q");

        String srcPath = "Q\\src";
        String destPath = "Q\\dest";

        // Should not throw
        assertDoesNotThrow(() -> worker.move(srcPath, destPath));
       
        // After move, src should be under dest
        FileSystemEntity dest = (FileSystemEntity) worker.getDrives().get("Q").getChild("dest");
        assertNotNull(dest.getChild("src"));
        
        assertDoesNotThrow(() -> worker.move(destPath, "EXT\\"));
        
     // After move, dest should be under new Drive EXT
        dest = (FileSystemEntity) worker.getDrives().get("EXT");
        assertNotNull(dest.getChild("dest"));
    }

    static Stream<Arguments> invalidMoveCases() {
        return Stream.of(
            Arguments.of("invalidSrc", "C\\docs"),
            Arguments.of("C\\docs\\file.txt", "invalidDest"),
            Arguments.of("invalidSrc", "invalidDest"),
            // Source is a file, not a file system
            Arguments.of("C\\docs\\file.txt", "C\\docs\\file.txt"),
            Arguments.of("C\\docs\\", "C\\docs\\file.txt"),
            Arguments.of("C\\docs\\file.txt", "C\\docs\\"),
            Arguments.of("C\\", "D")
        );
    }
    
    @ParameterizedTest
    @MethodSource("invalidMoveCases")
    void moveToNonFileSystem_throws(String srcPath, String destPath) {
    	worker.create(EntityType.DRIVE, "C", null);
        assertThrows(Exception.class, () -> worker.move(srcPath, destPath));
    }

    @Test
    void writeToFile_success() {
        worker.create(EntityType.DRIVE, "K", null);
        worker.create(EntityType.TEXT_FILE, "diary.txt", "K");
        
        worker.writeToFile("K\\diary.txt", "Today was a good day.");
        
        TextFile txtFile = (TextFile)FSUtils.findEntity("K\\diary.txt", worker.getDrives());
        
        assertNotNull(txtFile);
        assertEquals("Today was a good day.", txtFile.getContent());
    }

    @Test
    void writeToNonTextFile_throws() {
        worker.create(EntityType.DRIVE, "L", null);
        worker.create(EntityType.FOLDER, "music", "L");
        InvalidOperationException ex = assertThrows(InvalidOperationException.class, () ->
                worker.writeToFile("L\\music", "Should fail"));
        assertEquals("Not a text file", ex.getMessage());
    }

    @Test
    void renameFile_success() {
        worker.create(EntityType.DRIVE, "M", null);
        worker.create(EntityType.TEXT_FILE, "draft.txt", "M");
        worker.rename("M\\draft.txt", "final.txt");
    }

    @Test
    void renameDrive_throws() {
        worker.create(EntityType.DRIVE, "N", null);
        InvalidOperationException ex = assertThrows(InvalidOperationException.class, () ->
                worker.rename("N", "O"));
        assertEquals("Cannot rename drive", ex.getMessage());
    }

    @Test
    void renameToExistingName_throws() {
        worker.create(EntityType.DRIVE, "P", null);
        worker.create(EntityType.TEXT_FILE, "report.txt", "P");
        worker.create(EntityType.TEXT_FILE, "summary.txt", "P");
        NameConflictException ex = assertThrows(NameConflictException.class, () ->
                worker.rename("P\\report.txt", "summary.txt"));
        assertTrue(ex.getMessage().contains("already exists"));
    }
    
    
    
    static Stream<Arguments> deleteCases() {
        return Stream.of(
            Arguments.of("A\\docs\\file1.txt", "A\\docs", "file1.txt"),
            Arguments.of("A\\docs\\sub\\file2.txt", "A\\docs\\sub", "file2.txt"),
            Arguments.of("A\\docs", "A", "docs"),
            Arguments.of("B", null, "B")
        );
    }

    @ParameterizedTest
    @MethodSource("deleteCases")
    void deleteEntity_removesCorrectly(String deletePath, String parentPath, String deletedName) {
    	worker.create(EntityType.DRIVE, "A", null);
        worker.create(EntityType.DRIVE, "B", null);
        worker.create(EntityType.FOLDER, "docs", "A");
        worker.create(EntityType.TEXT_FILE, "file1.txt", "A\\docs");
        worker.create(EntityType.FOLDER, "sub", "A\\docs");
        worker.create(EntityType.TEXT_FILE, "file2.txt", "A\\docs\\sub");
        worker.delete(deletePath);
        if (parentPath == null) {
            assertNull(worker.getDrives().get(deletedName));
        } else {
            FileSystemEntity parent = (FileSystemEntity)FSUtils.findEntity(parentPath, worker.getDrives());
            
            assertThrows(EntityNotFoundException.class, () -> parent.getChild(deletedName));
        }
    }

    static Stream<Arguments> deleteInvalidCases() {
        return Stream.of(
            Arguments.of("A\\notfound"),
            Arguments.of("C\\docs")
        );
    }

    @ParameterizedTest
    @MethodSource("deleteInvalidCases")
    void deleteNonExistent_throws(String path) {
        assertThrows(EntityNotFoundException.class, () -> worker.delete(path));
    }

}
