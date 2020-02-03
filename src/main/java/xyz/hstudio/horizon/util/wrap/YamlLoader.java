package xyz.hstudio.horizon.util.wrap;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.file.YamlConstructor;
import org.bukkit.configuration.file.YamlRepresenter;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.error.YAMLException;
import org.yaml.snakeyaml.representer.Representer;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.logging.Level;

public class YamlLoader extends YamlConfiguration {

    public int commentNum = 0;
    private DumperOptions yamlOptions = new DumperOptions();
    private Representer yamlRepresenter = new YamlRepresenter();
    private Yaml yaml;

    private YamlLoader() {
        this.yaml = new Yaml(new YamlConstructor(), this.yamlRepresenter, this.yamlOptions);
    }

    public static YamlLoader loadConfiguration(final File file) {
        YamlLoader config = new YamlLoader();
        try {
            config.load(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8));
        } catch (FileNotFoundException ignored) {
        } catch (IOException | InvalidConfigurationException var4) {
            Bukkit.getLogger().log(Level.SEVERE, "Cannot load " + file, var4);
        }
        return config;
    }

    public static YamlConfiguration loadConfiguration(final InputStream stream) {
        YamlLoader config = new YamlLoader();
        try {
            config.load(new InputStreamReader(stream));
        } catch (IOException | InvalidConfigurationException var3) {
            Bukkit.getLogger().log(Level.SEVERE, "Cannot load configuration from stream", var3);
        }
        return config;
    }

    public void load(final Reader reader) throws IOException, InvalidConfigurationException {
        BufferedReader input = reader instanceof BufferedReader ? (BufferedReader) reader : new BufferedReader(reader);
        StringBuilder builder = new StringBuilder();
        String line;
        while ((line = input.readLine()) != null) {
            builder.append(line);
            builder.append('\n');
        }
        loadFromString(this.getConfigContent(builder.toString()));
    }

    @SuppressWarnings("rawtypes")
    public void loadFromString(final String contents) throws InvalidConfigurationException {
        Map input;
        try {
            input = (Map) yaml.load(contents);
        } catch (YAMLException var4) {
            throw new InvalidConfigurationException(var4);
        } catch (ClassCastException var5) {
            throw new InvalidConfigurationException("Top level is not a Map.");
        }
        String header = parseHeader(contents);
        if (header.length() > 0) {
            options().header(header);
        }
        if (input != null) {
            convertMapsToSections(input, this);
        }
    }

    private String getConfigContent(final String string) {
        try {
            String addLine;
            String currentLine;
            StringBuilder whole = new StringBuilder();
            BufferedReader reader = new BufferedReader(new StringReader(string));
            while ((currentLine = reader.readLine()) != null) {
                if (currentLine.trim().startsWith("#")) {
                    addLine = currentLine.replaceFirst("#", "COMMENT_" + commentNum + ": \"");
                    whole.append(addLine).append("\"").append("\n");
                    commentNum++;
                } else {
                    whole.append(currentLine).append("\n");
                }
            }
            reader.close();
            return whole.toString();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void save(final File file) throws IOException {
        String data = this.prepareConfigString(this.saveToString());
        try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) {
            writer.write(data);
        }
    }

    public String saveToString() {
        this.yamlOptions.setIndent(options().indent());
        this.yamlOptions.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        this.yamlRepresenter.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        this.yamlOptions.setSplitLines(false);
        String header = buildHeader();
        String dump = yaml.dump(getValues(false));
        if (dump.equals("{}\n")) {
            dump = "";
        }
        return header + dump;
    }

    private String prepareConfigString(final String configString) {
        int lastLine = 0;
        int headerLine = 0;
        String[] lines = configString.split("\n");
        StringBuilder config = new StringBuilder();
        for (String line : lines) {
            if (line.trim().startsWith("COMMENT")) {
                int count = line.replaceAll("([ ]*).*", "$1").length();
                StringBuilder space = new StringBuilder();
                for (int i = 0; i < count; i++) {
                    space.append(" ");
                }
                String comment = space.toString() + "#" + line.substring(line.indexOf(":") + 3);
                comment = comment.substring(0, comment.length() - 1);
                if (comment.startsWith("# +-")) {
                    if (headerLine == 0) {
                        config.append(comment).append("\n");
                        lastLine = 0;
                        headerLine = 1;
                    } else {
                        config.append(comment).append("\n\n");
                        lastLine = 0;
                        headerLine = 0;
                    }
                } else {
                    String normalComment;
                    if (comment.startsWith("# ' ")) {
                        normalComment = comment.substring(0, comment.length() - 1).replaceFirst("# ' ", "# ");
                    } else {
                        normalComment = comment;
                    }
                    if (lastLine == 0) {
                        config.append(normalComment).append("\n");
                    } else {
                        config.append("\n").append(normalComment).append("\n");
                    }
                    lastLine = 0;
                }
            } else {
                config.append(line).append("\n");
                lastLine = 1;
            }
        }
        return config.toString();
    }

    public ConfigurationSection getSection(final String path) {
        Object val = this.get(path, null);
        if (val != null) {
            return val instanceof ConfigurationSection ? (ConfigurationSection) val : null;
        } else {
            val = this.get(path, this.getDefault(path));
            return val instanceof ConfigurationSection ? this.createSection(path) : null;
        }
    }
}