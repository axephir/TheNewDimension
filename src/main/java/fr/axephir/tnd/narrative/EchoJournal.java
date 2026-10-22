package fr.axephir.tnd.narrative;

import net.minecraft.nbt.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.*;
import java.util.*;

/** Two independently verified, durable reference files. No reference or source chunk is ever deleted by the story. */
public final class EchoJournal {
    public static final int MAX_BYTES=32*1024*1024;
    private final Path directory;
    public EchoJournal(MinecraftServer server,UUID player,UUID session){this(server.getWorldPath(LevelResource.ROOT).resolve("data/tnd-convergence").resolve(player.toString()).resolve(session.toString()));}
    public EchoJournal(Path directory){this.directory=directory;}
    public Path directory(){return directory;}
    public void prepare(CompoundTag reference) throws IOException {
        ByteArrayOutputStream out=new ByteArrayOutputStream();NbtIo.writeCompressed(reference,out);byte[] bytes=out.toByteArray();if(bytes.length>MAX_BYTES)throw new IOException("Story reference exceeds bounded journal size");
        Files.createDirectories(directory);byte[] checksum=sha(bytes).getBytes(StandardCharsets.US_ASCII);
        atomic("reference-a.nbt",bytes);atomic("reference-a.sha256",checksum);atomic("reference-b.nbt",bytes);atomic("reference-b.sha256",checksum);
        if(!readCopy("a").equals(reference)||!readCopy("b").equals(reference))throw new IOException("Independent story references did not verify");
        mark("prepared");
    }
    public CompoundTag read() throws IOException {IOException a;try{return readCopy("a");}catch(IOException failure){a=failure;}try{return readCopy("b");}catch(IOException failure){failure.addSuppressed(a);throw failure;}}
    private CompoundTag readCopy(String suffix) throws IOException {
        var path=directory.resolve("reference-"+suffix+".nbt");if(Files.size(path)>MAX_BYTES)throw new IOException("Oversized story reference");byte[] bytes=Files.readAllBytes(path);
        String expected=Files.readString(directory.resolve("reference-"+suffix+".sha256"),StandardCharsets.US_ASCII).strip();if(expected.length()!=64||!MessageDigest.isEqual(sha(bytes).getBytes(StandardCharsets.US_ASCII),expected.getBytes(StandardCharsets.US_ASCII)))throw new IOException("Story reference checksum mismatch");
        try{return NbtIo.readCompressed(new ByteArrayInputStream(bytes),NbtAccounter.create(MAX_BYTES));}catch(RuntimeException failure){throw new IOException("Invalid story reference",failure);}
    }
    public void mark(String state) throws IOException {atomic("phase.txt",state.getBytes(StandardCharsets.US_ASCII));}
    private void atomic(String name,byte[] bytes) throws IOException {
        Files.createDirectories(directory);var destination=directory.resolve(name);var temporary=directory.resolve(name+".pending");
        try(var channel=FileChannel.open(temporary,StandardOpenOption.CREATE,StandardOpenOption.TRUNCATE_EXISTING,StandardOpenOption.WRITE)){var buffer=ByteBuffer.wrap(bytes);while(buffer.hasRemaining())channel.write(buffer);channel.force(true);}
        Files.move(temporary,destination,StandardCopyOption.ATOMIC_MOVE,StandardCopyOption.REPLACE_EXISTING);
        // Reopen the final name after the atomic move as well. Windows does not expose
        // directory channels, but still supports forcing the independently verified files.
        try(var channel=FileChannel.open(destination,StandardOpenOption.WRITE)){channel.force(true);}
        forceDirectory(directory);
    }
    private static void forceDirectory(Path directory) throws IOException {
        if(System.getProperty("os.name","").toLowerCase(Locale.ROOT).startsWith("windows"))return;
        try(var channel=FileChannel.open(directory,StandardOpenOption.READ)){channel.force(true);}
    }
    public static boolean plotReserved(MinecraftServer server,int slot){return Files.exists(server.getWorldPath(LevelResource.ROOT).resolve("data/tnd-convergence/plots").resolve(slot+".reserved"));}
    public static void reservePlot(MinecraftServer server,int slot,UUID owner,UUID session) throws IOException {
        Path base=server.getWorldPath(LevelResource.ROOT).resolve("data/tnd-convergence/plots");Files.createDirectories(base);
        try(var file=FileChannel.open(base.resolve(slot+".reserved"),StandardOpenOption.CREATE_NEW,StandardOpenOption.WRITE)){var bytes=ByteBuffer.wrap((owner+";"+session).getBytes(StandardCharsets.US_ASCII));while(bytes.hasRemaining())file.write(bytes);file.force(true);}
        forceDirectory(base);
    }
    /** Independent of both player attachments and SavedData. Search only this owner's bounded journal directory and matching projection. */
    public static EchoReference.State discover(MinecraftServer server,net.minecraft.server.level.ServerPlayer player){
        Path base=server.getWorldPath(LevelResource.ROOT).resolve("data/tnd-convergence").resolve(player.getUUID().toString());if(!Files.isDirectory(base))return null;
        try(var entries=Files.list(base)){
            for(Path path:entries.sorted(Comparator.comparing(Path::toString).reversed()).limit(2048).toList()){
                try{UUID session=UUID.fromString(path.getFileName().toString());var journal=new EchoJournal(path);var state=EchoReference.validate(journal.read(),player.getUUID(),session);
                    if(Math.abs(player.getX()-EchoCopyTask.plotX(state.slot()))<1024&&Math.abs(player.getZ()-EchoCopyTask.plotZ(state.slot()))<1024)return state;
                }catch(IOException|RuntimeException invalid){/* Retain damaged journals, try the next independently owned reference. */}
            }
        }catch(IOException failure){org.slf4j.LoggerFactory.getLogger("tnd/convergence").error("Could not enumerate retained recovery journals",failure);}return null;
    }
    private static String sha(byte[] bytes) throws IOException {try{return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));}catch(NoSuchAlgorithmException e){throw new IOException(e);}}
}
