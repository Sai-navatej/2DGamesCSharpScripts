// Source code is decompiled from a .class file using FernFlower decompiler (from Intellij IDEA).
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.ProcessBuilder.Redirect;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import java.util.Set;

public class Main {
   private static final List<Job> activeJobs = new ArrayList();

   public Main() {
   }

   private static void sortJobs() {
      activeJobs.sort(Comparator.comparingInt((var0) -> var0.jobId));
   }

   private static String getMarker(Job var0) {
      int var1 = activeJobs.indexOf(var0);
      if (var1 == -1) {
         return " ";
      } else if (var1 == activeJobs.size() - 1) {
         return "+";
      } else {
         return var1 == activeJobs.size() - 2 ? "-" : " ";
      }
   }

   private static void printJob(Job var0, String var1, String var2) {
      String var3 = String.format("%-24s", var2);
      String var4 = var0.command;
      if (var2.equals("Running")) {
         var4 = var4 + " &";
      }

      System.out.printf("[%d]%s  %s%s%n", var0.jobId, var1, var3, var4);
   }

   private static void reapFinishedJobs() {
      sortJobs();
      ArrayList var0 = new ArrayList();

      for(Job var2 : activeJobs) {
         if (!var2.process.isAlive()) {
            var0.add(var2);
         }
      }

      for(Job var5 : var0) {
         String var3 = getMarker(var5);
         printJob(var5, var3, "Done");
         activeJobs.remove(var5);
      }

   }

   private static void listJobs() {
      sortJobs();
      ArrayList var0 = new ArrayList();

      for(Job var2 : activeJobs) {
         String var3 = getMarker(var2);
         if (var2.process.isAlive()) {
            printJob(var2, var3, "Running");
         } else {
            printJob(var2, var3, "Done");
            var0.add(var2);
         }
      }

      activeJobs.removeAll(var0);
   }

   private static Optional<Path> getValidPath(String var0) {
      String[] var1 = ((String)Optional.ofNullable(System.getenv("PATH")).orElse("")).split(":");

      for(String var6 : var1) {
         Optional var2 = getFile(var0, var6);
         if (var2.isPresent()) {
            return var2;
         }
      }

      return Optional.empty();
   }

   private static Optional<Path> getFile(String var0, String var1) {
      Path var2 = Paths.get(var1, var0);
      return var2.toFile().exists() && var2.toFile().canExecute() ? Optional.of(var2) : Optional.empty();
   }

   public static void executeScript(Path var0, List<String> var1, String var2, boolean var3, String var4, boolean var5, boolean var6, String var7) {
      ProcessBuilder var8 = new ProcessBuilder(new String[0]);
      var8.command(var1);
      var8.directory(new File(var0.getParent().toString()));
      if (var2 != null) {
         File var9 = new File(var2);
         File var10 = var9.getParentFile();
         if (var10 != null && !var10.exists()) {
            var10.mkdirs();
         }

         if (var3) {
            var8.redirectOutput(Redirect.appendTo(var9));
         } else {
            var8.redirectOutput(Redirect.to(var9));
         }
      } else {
         var8.redirectOutput(Redirect.INHERIT);
      }

      if (var4 != null) {
         File var15 = new File(var4);
         File var17 = var15.getParentFile();
         if (var17 != null && !var17.exists()) {
            var17.mkdirs();
         }

         if (var5) {
            var8.redirectError(Redirect.appendTo(var15));
         } else {
            var8.redirectError(Redirect.to(var15));
         }
      } else {
         var8.redirectError(Redirect.INHERIT);
      }

      var8.redirectInput(Redirect.INHERIT);

      try {
         Process var16 = var8.start();
         if (var6) {
            int var18;
            for(var18 = 1; !activeJobs.stream().noneMatch((var1x) -> var1x.jobId == var18); ++var18) {
            }

            long var11 = var16.pid();
            Job var13 = new Job(var18, var16, var11, var7);
            activeJobs.add(var13);
            System.out.printf("[%d] %d%n", var18, var11);
         } else {
            var16.waitFor();
         }

      } catch (InterruptedException | IOException var14) {
         throw new RuntimeException(var14);
      }
   }

   public static void main(String[] var0) {
      HashSet var1 = new HashSet();
      fillBuiltinCommands(var1);
      Path var2 = Paths.get("");
      Scanner var3 = new Scanner(System.in);

      while(true) {
         reapFinishedJobs();
         System.out.print("$ ");
         String var4 = var3.nextLine();
         List var5 = tokenize(var4);
         if (!var5.isEmpty()) {
            String var6 = null;
            boolean var7 = false;
            String var8 = null;
            boolean var9 = false;
            ArrayList var10 = new ArrayList();

            for(int var11 = 0; var11 < var5.size(); ++var11) {
               String var12 = (String)var5.get(var11);
               if (!var12.equals(">") && !var12.equals("1>")) {
                  if (!var12.equals(">>") && !var12.equals("1>>")) {
                     if (var12.equals("2>")) {
                        if (var11 + 1 < var5.size()) {
                           var8 = (String)var5.get(var11 + 1);
                           var9 = false;
                           ++var11;
                        }
                     } else if (var12.equals("2>>")) {
                        if (var11 + 1 < var5.size()) {
                           var8 = (String)var5.get(var11 + 1);
                           var9 = true;
                           ++var11;
                        }
                     } else {
                        var10.add(var12);
                     }
                  } else if (var11 + 1 < var5.size()) {
                     var6 = (String)var5.get(var11 + 1);
                     var7 = true;
                     ++var11;
                  }
               } else if (var11 + 1 < var5.size()) {
                  var6 = (String)var5.get(var11 + 1);
                  var7 = false;
                  ++var11;
               }
            }

            if (!var10.isEmpty()) {
               boolean var27 = false;
               String var28 = (String)var10.getLast();
               if (var28.equals("&")) {
                  var27 = true;
                  var10.removeLast();
               } else if (var28.endsWith("&")) {
                  var27 = true;
                  var10.set(var10.size() - 1, var28.substring(0, var28.length() - 1));
               }

               if (!var10.isEmpty()) {
                  String var13 = String.join(" ", var10);
                  String var14 = (String)var10.getFirst();
                  String var15 = var10.size() > 1 ? (String)var10.get(1) : null;
                  PrintStream var16 = System.out;
                  PrintStream var17 = System.err;
                  PrintStream var18 = null;
                  PrintStream var19 = null;

                  try {
                     if (var6 != null) {
                        File var20 = new File(var6);
                        File var21 = var20.getParentFile();
                        if (var21 != null && !var21.exists()) {
                           var21.mkdirs();
                        }

                        var18 = new PrintStream(new FileOutputStream(var20, var7));
                        System.setOut(var18);
                     }

                     if (var8 != null) {
                        File var29 = new File(var8);
                        File var30 = var29.getParentFile();
                        if (var30 != null && !var30.exists()) {
                           var30.mkdirs();
                        }

                        var19 = new PrintStream(new FileOutputStream(var29, var9));
                        System.setErr(var19);
                     }

                     switch (var14) {
                        case "exit":
                           System.exit(0);
                           break;
                        case "echo":
                           echo(var10);
                           break;
                        case "type":
                           type(var1, var15);
                           break;
                        case "pwd":
                           System.out.println(var2.toAbsolutePath());
                           break;
                        case "cd":
                           var2 = changeDirectory(var15, var2);
                           break;
                        case "jobs":
                           listJobs();
                           break;
                        default:
                           execute(var10, var6, var7, var8, var9, var27, var13);
                     }
                  } catch (IOException var25) {
                     System.err.println(var25.getMessage());
                  } finally {
                     if (var18 != null) {
                        var18.close();
                        System.setOut(var16);
                     }

                     if (var19 != null) {
                        var19.close();
                        System.setErr(var17);
                     }

                  }
               }
            }
         }
      }
   }

   private static void echo(List<String> var0) {
      for(int var1 = 1; var1 < var0.size(); ++var1) {
         System.out.print((String)var0.get(var1));
         if (var1 < var0.size() - 1) {
            System.out.print(" ");
         }
      }

      System.out.println();
   }

   private static List<String> tokenize(String var0) {
      ArrayList var1 = new ArrayList();
      StringBuilder var2 = new StringBuilder();
      boolean var3 = false;
      boolean var4 = false;
      boolean var5 = false;

      for(int var6 = 0; var6 < var0.length(); ++var6) {
         char var7 = var0.charAt(var6);
         if (var3) {
            if (var7 == '\'') {
               var3 = false;
               var5 = true;
            } else {
               var2.append(var7);
               var5 = true;
            }
         } else if (var4) {
            if (var7 == '"') {
               var4 = false;
               var5 = true;
            } else if (var7 == '\\') {
               if (var6 + 1 < var0.length()) {
                  char var8 = var0.charAt(var6 + 1);
                  if (var8 != '\\' && var8 != '"' && var8 != '$' && var8 != '`') {
                     var2.append(var7);
                  } else {
                     var2.append(var8);
                     ++var6;
                  }
               } else {
                  var2.append(var7);
               }

               var5 = true;
            } else {
               var2.append(var7);
               var5 = true;
            }
         } else if (var7 == '\'') {
            var3 = true;
            var5 = true;
         } else if (var7 == '"') {
            var4 = true;
            var5 = true;
         } else if (var7 == '\\') {
            if (var6 + 1 < var0.length()) {
               var2.append(var0.charAt(var6 + 1));
               ++var6;
            } else {
               var2.append(var7);
            }

            var5 = true;
         } else if (var7 == ' ') {
            if (var5 || !var2.isEmpty()) {
               var1.add(var2.toString());
               var2.setLength(0);
               var5 = false;
            }
         } else {
            var2.append(var7);
            var5 = true;
         }
      }

      if (var5 || !var2.isEmpty()) {
         var1.add(var2.toString());
      }

      return var1;
   }

   private static void fillBuiltinCommands(Set<String> var0) {
      var0.add("exit");
      var0.add("echo");
      var0.add("type");
      var0.add("pwd");
      var0.add("cd");
      var0.add("jobs");
   }

   private static void execute(List<String> var0, String var1, boolean var2, String var3, boolean var4, boolean var5, String var6) {
      getValidPath((String)var0.getFirst()).ifPresentOrElse((var7) -> executeScript(var7, var0, var1, var2, var3, var4, var5, var6), () -> System.err.printf("%s: command not found%n", var0.getFirst()));
   }

   private static void type(Set<String> var0, String var1) {
      if (var0.contains(var1)) {
         System.out.printf("%s is a shell builtin%n", var1);
      } else {
         Optional var2 = getValidPath(var1);
         if (var2.isPresent()) {
            System.out.printf("%s is %s%n", var1, var2.get());
         } else {
            System.out.printf("%s: not found%n", var1);
         }
      }
   }

   private static Path changeDirectory(String var0, Path var1) {
      if (var0 == null) {
         return var1;
      } else {
         if ("~".equals(var0.strip())) {
            var0 = System.getenv("HOME");
         }

         Path var2 = var1.resolve(var0).toAbsolutePath().normalize();
         if (var2.toFile().exists()) {
            var1 = var2;
         } else {
            System.out.printf("cd: %s: No such file or directory%n", var2.toAbsolutePath());
         }

         return var1;
      }
   }

   static class Job {
      int jobId;
      Process process;
      long pid;
      String command;

      Job(int var1, Process var2, long var3, String var5) {
         this.jobId = var1;
         this.process = var2;
         this.pid = var3;
         this.command = var5;
      }
   }
}
