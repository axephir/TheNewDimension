import com.sun.source.util.JavacTask;
import javax.tools.*;
import java.nio.file.*;
import java.util.*;

/** Parse only. This intentionally does not claim to compile Minecraft-dependent code. */
public class CheckJavaSyntax {
    public static void main(String[] args) throws Exception {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null) throw new IllegalStateException("A JDK is required");
        var diagnostics = new DiagnosticCollector<JavaFileObject>();
        try (var manager = compiler.getStandardFileManager(diagnostics, Locale.ROOT, java.nio.charset.StandardCharsets.UTF_8);
             var paths = Files.walk(Path.of("src"))) {
            var files = paths.filter(p -> p.toString().endsWith(".java")).toList();
            JavacTask task = (JavacTask) compiler.getTask(null, manager, diagnostics,
                    List.of("-proc:none", "--release", "25"), null, manager.getJavaFileObjectsFromPaths(files));
            task.parse();
            boolean error = false;
            for (var diagnostic : diagnostics.getDiagnostics()) {
                System.out.println(diagnostic);
                if (diagnostic.getKind() == Diagnostic.Kind.ERROR) error = true;
            }
            if (error) throw new IllegalStateException("Java syntax errors");
            System.out.println("Java syntax parsed: " + files.size() + " files (types/API compatibility NOT checked)");
        }
    }
}
