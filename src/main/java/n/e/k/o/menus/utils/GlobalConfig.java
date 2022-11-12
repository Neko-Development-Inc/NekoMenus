package n.e.k.o.menus.utils;

import org.apache.logging.log4j.Logger;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class GlobalConfig {

    public static boolean checkEnabled(String modName, Logger logger) {
        try {
            File folder = NekoFolder.getOrCreateConfigFolder(modName);
            if (folder == null)
                return false;
            File globalConfigFile = new File(folder.getParentFile(), "global.yml");
            if (!globalConfigFile.exists()) createNewFile(modName, globalConfigFile);
            else {
                List<String> out = new ArrayList<>();
                List<String> lines = Files.readAllLines(globalConfigFile.toPath());
                int lineNumber = 0;
                var foundSelf = false;
                String selfState = "";
                for (String line : lines) {
                    lineNumber++;
                    if (line.startsWith("#") || line.equals("mods:") || line.isEmpty()) {
                        out.add(line);
                        continue;
                    }
                    else if (line.startsWith(" - ")) {
                        var data = line.substring(3);
                        var split = data.split(Pattern.quote(":"));
                        if (split.length == 2) {
                            var name = split[0].trim();
                            var state = split[1].trim();
                            if (!foundSelf && name.equalsIgnoreCase(modName)) {
                                foundSelf = true;
                                selfState = state;
                            }
                            out.add(" - " + name + ": " + state);
                            continue;
                        }
                    }
                    logger.error("Invalid string found at line " + lineNumber + ": `" + line + "`");
                }
                if (!foundSelf) {
                    int emptyLines = 0;
                    for (int i = out.size() - 1; i >= 0; i--) {
                        var line = out.get(i);
                        if (line.isEmpty()) {
                            out.remove(i);
                            emptyLines++;
                        } else
                            break;
                    }
                    out.add(" - " + modName + ": enabled");
                    for (int i = 0; i < emptyLines; i++)
                        out.add("");
                    Files.writeString(globalConfigFile.toPath(), String.join("\n", out));
                    return true;
                }
                while (selfState.contains("'") || selfState.contains("\""))
                    selfState = selfState.replace("'", "").replace("\"", "");
                return selfState.equals("enabled") || selfState.equals("true") || selfState.equals("1");
            }
        } catch (Throwable t) {
            logger.error("Error reading/writing to global config.", t);
        }
        return true;
    }

    private static void createNewFile(String modName, File globalConfigFile) throws Throwable {
        StringBuilder sb = new StringBuilder();
        sb.append("# Global config for all Neko Development mods.\n");
        sb.append("# For the state, you can use either: 'enabled' / 'disabled', 'true' / 'false', or '1' / '0', without the quotes.\n\n");
        sb.append("mods:\n").append(" - ").append(modName).append(": enabled");
        Files.writeString(globalConfigFile.toPath(), sb.toString());
    }

}
