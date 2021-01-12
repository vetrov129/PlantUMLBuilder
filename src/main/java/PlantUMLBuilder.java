import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;

public class PlantUMLBuilder {

    private static final String PATH_TO_CLASSES = "C:\\Users\\vetro\\IdeaProjects\\CPSemester3\\CPServer\\src\\main\\java";
    private static final String PATH_TO_NEW_FILE = "C:\\Users\\vetro\\IdeaProjects\\CPSemester3\\CPServer\\src\\main\\resources";

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        new PlantUMLBuilder().buildUML();
    }

    private void buildUML() throws ClassNotFoundException, IOException {
        ArrayList<Class<?>> classes = getClassesInDirectory(new File(PATH_TO_CLASSES));
        FileOutputStream fos = createFileAdnGetStream(PATH_TO_NEW_FILE, "uml");
        for (Class<?> clazz : classes) {
            if (clazz.isAnnotation()){
                writeAnnotationDescription(fos, clazz);
            }
            else {
                writeClassDescription(fos, clazz);
            }
        }
        closeFile(fos);
    }

    private void writeAnnotationDescription(FileOutputStream fos, Class<?> clazz) throws IOException {
        fos.write(("annotation " + clazz.getSimpleName() + "\n").getBytes());
        writeAnnotationMethods(fos, clazz);
    }

    private void writeAnnotationMethods(FileOutputStream fos, Class<?> clazz) throws IOException {
        for (Method m : clazz.getDeclaredMethods()) {
            fos.write((
                    "\t" + clazz.getSimpleName() + " : " +
                    m.getName() + "() : " +
                    m.getReturnType().getSimpleName() + "\n"
            ).getBytes());
        }
        fos.write("\n".getBytes());
    }

    private void writeClassDescription(FileOutputStream fos, Class<?> clazz) throws IOException {
        fos.write((getClassType(clazz) + " " + clazz.getSimpleName() + " {\n").getBytes());
        writeClassFields(fos, clazz);
        writeClassMethods(fos, clazz);
        fos.write("} \n".getBytes());
        writeClassInheritance(fos, clazz);
    }

    private String getClassType(Class<?> clazz) {
        if (clazz.isEnum()) return "enum";
        if (clazz.isInterface()) return "interface";
        if (Modifier.isAbstract(clazz.getModifiers())) return "abstract class";
        return "class";
    }
    private void writeClassFields(FileOutputStream fos, Class<?> clazz) throws IOException {
        for (Field f : clazz.getDeclaredFields()) {
            if (f.getName().startsWith("$")) {
                continue;
            }
            fos.write((
                    "\t" +
                    getUMLModifiers(f.getModifiers()) +
                    f.getName() + " : " +
                    f.getType().getSimpleName() + "\n"
            ).getBytes());
        }
        fos.write("\n".getBytes());
    }

    private void writeClassMethods(FileOutputStream fos, Class<?> clazz) throws IOException {
        for (Method m : clazz.getDeclaredMethods()) {
            if (m.getName().startsWith("lambda$")) {
                continue;
            }
            fos.write((
                    "\t" +
                    getUMLModifiers(m.getModifiers()) +
                    m.getName() + "() : " +
                    m.getReturnType().getSimpleName() + "\n"
            ).getBytes());
        }
        fos.write("\n".getBytes());
    }

    private void writeClassInheritance(FileOutputStream fos, Class<?> clazz) throws IOException {
        if (clazz.getSuperclass() != null && !clazz.getSuperclass().equals(Object.class)) {
            fos.write((clazz.getSimpleName() + " ----|> " + clazz.getSuperclass().getSimpleName() + "\n").getBytes());
        }
        fos.write("\n".getBytes());
    }

    private String getUMLModifiers (int modifiers) {
        StringBuilder modifiersSB = new StringBuilder();
        if (Modifier.isPrivate(modifiers)) modifiersSB.append("- ");
        if (Modifier.isPublic(modifiers)) modifiersSB.append("+ ");
        if (Modifier.isProtected(modifiers)) modifiersSB.append("# ");
        if (Modifier.isStatic(modifiers)) modifiersSB.append("{static} ");
        return modifiersSB.toString();
    }

    private FileOutputStream createFileAdnGetStream(String path, String name) throws IOException {
        File file = new File(path + File.separatorChar + name + ".puml");
        FileOutputStream fos = new FileOutputStream(file);
        fos.write("@startuml\n\n".getBytes());
        return fos;

    }

    private void closeFile(FileOutputStream fos) throws IOException {
        fos.write("\n\n@enduml".getBytes());
        fos.close();
    }

    public ArrayList<Class<?>> getClassesInDirectory(File directory) throws ClassNotFoundException {
        ArrayList<File> files = getFilesFromDirectory(directory);
        ArrayList<Class<?>> resultClasses = new ArrayList<>();
        for (File contentItem : files) {
            if (contentItem.getName().endsWith(".java")) {
                String className = contentItem.getAbsolutePath()
                        .replace(directory.getAbsolutePath(), "")
                        .replace(File.separatorChar, '.');
                className = className.substring(1, className.length() - 5);
                resultClasses.add(Class.forName(className));
            }
        }
        return resultClasses;
    }

    private ArrayList<File> getFilesFromDirectory(File directory) {
        if (directory == null) {
            throw new IllegalArgumentException("directory cannot be null");
        }
        if (!directory.isDirectory()) {
            throw new IllegalArgumentException("invalid directory + \"" + directory.getAbsolutePath() + "\"");
        }
        ArrayList<File> result = new ArrayList<>();
        for (File file : directory.listFiles()) {
            if (file.isDirectory()) {
                result.addAll(getFilesFromDirectory(file));
            } else {
                result.add(file);
            }
        }
        return result;
    }
}
