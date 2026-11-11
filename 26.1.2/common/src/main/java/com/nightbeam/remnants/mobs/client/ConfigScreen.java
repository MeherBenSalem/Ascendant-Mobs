package com.nightbeam.remnants.mobs.client;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.nightbeam.remnants.mobs.RemnantMobs;
import com.nightbeam.remnants.mobs.config.RemnantsConfig;
import com.nightbeam.remnants.mobs.config.edit.ConfigEdit;
import com.nightbeam.remnants.mobs.config.edit.ConfigFiles;
import com.nightbeam.remnants.mobs.config.edit.ConfigOption;
import com.nightbeam.remnants.mobs.config.edit.ConfigOption.Kind;
import com.nightbeam.remnants.mobs.config.edit.ConfigOption.Side;
import com.nightbeam.remnants.mobs.config.edit.ConfigOptions;
import com.nightbeam.remnants.mobs.config.edit.ConfigPaths;
import com.nightbeam.remnants.mobs.network.ConfigRequestPayload;
import com.nightbeam.remnants.mobs.network.ConfigSync;
import com.nightbeam.remnants.mobs.network.ConfigUpdatePayload;
import com.nightbeam.remnants.mobs.network.ConfigValuesPayload;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.FormattedCharSequence;
import org.jspecify.annotations.Nullable;

/**
 * The settings screen for difficulty and scaling. It edits the JSON files in {@code config/remnant_mobs/} through the
 * same specs and rules as the loader (see {@code config/edit}): with no world open it edits this computer's files; in
 * a world it shows the server's values, which only the host or an operator can change (the server checks again).
 * Settings that only change what this player sees are always saved to this computer's files.
 */
public final class ConfigScreen extends Screen {
    private static final int CONTROL_WIDTH = 110;
    private static final int ROW_HEIGHT = 24;
    private static final int GOLD = 0xFFE2B866;
    private static final int TEXT = 0xFFE6E1D3;
    private static final int MUTED = 0xFF9C958A;
    private static final int RED = 0xFFE0563C;
    private static final int GREEN = 0xFF7FD86A;

    /** No world open: this computer's files. */
    private static final int LOCAL = -3;
    private static final int UNAVAILABLE = -2;
    private static final int LOADING = -1;

    private final @Nullable Screen parent;
    private final boolean inWorld;
    private int access;
    private Map<String, JsonObject> local;
    private @Nullable Map<String, JsonObject> server;
    private ConfigEdit edit;
    private String group = ConfigOptions.GROUPS.get(0);
    private @Nullable OptionList list;
    private @Nullable Button save;
    private @Nullable Button done;
    private Component message = Component.empty();
    private int messageColor = MUTED;
    /** Set once the screen is shown and laid out; before that there is nothing to rebuild. */
    private boolean initialized;

    ConfigScreen(@Nullable Screen parent) {
        this(parent, true);
    }

    /** @param request ask the server for its values (false when they are already here: {@code /remnants config}) */
    ConfigScreen(@Nullable Screen parent, boolean request) {
        super(Component.translatable("config.remnant_mobs.title"));
        this.parent = parent;
        this.local = ConfigFiles.read(RemnantsConfig.dir());
        this.edit = edit(local);
        this.inWorld = Minecraft.getInstance().getConnection() != null;
        if (!inWorld) {
            access = LOCAL;
        } else if (!request) {
            access = LOADING;
        } else if (ConfigClient.canSend(ConfigRequestPayload.TYPE)) {
            access = LOADING;
            ConfigClient.send(new ConfigRequestPayload());
        } else {
            access = UNAVAILABLE;
        }
    }

    private static ConfigEdit edit(Map<String, JsonObject> values) {
        return new ConfigEdit(values, ConfigFiles.defaults(values.get("levels.json")));
    }

    /** The server's values (and what this player may do with them) arrived, first or after a save. */
    void serverValues(ConfigValuesPayload payload) {
        Map<String, JsonObject> files = new TreeMap<>();
        try {
            JsonObject root = JsonParser.parseString(payload.values()).getAsJsonObject();
            root.entrySet().forEach(e -> {
                if (e.getValue().isJsonObject()) {
                    files.put(e.getKey(), e.getValue().getAsJsonObject());
                }
            });
        } catch (RuntimeException e) {
            RemnantMobs.LOG.warn("Could not read the server's Remnants settings: {}", e.getMessage());
            return;
        }
        server = files;
        // In single player the integrated server has just written this computer's files too.
        local = ConfigFiles.read(RemnantsConfig.dir());
        if (!serverShown()) {
            // The server's sections were hidden until now: start at the first one.
            group = ConfigOptions.GROUPS.get(0);
        }
        access = payload.access();
        switch (payload.status()) {
            case ConfigSync.SAVED -> say(Component.translatable("config.remnant_mobs.saved", payload.count()), GREEN);
            case ConfigSync.PARTLY -> say(Component.translatable("config.remnant_mobs.refused",
                    payload.detail().isEmpty() ? 0 : payload.detail().split(", ").length, payload.detail()), RED);
            case ConfigSync.DENIED -> say(Component.translatable("config.remnant_mobs.denied"), RED);
            case ConfigSync.FAILED -> say(Component.translatable("config.remnant_mobs.failed", payload.detail()), RED);
            default -> {
            }
        }
        rebuildEdit();
    }

    /** Server values from the server, this player's own settings from this computer; unsaved own settings are kept. */
    private void rebuildEdit() {
        Map<String, Map<String, JsonElement>> pending = edit.changes(Side.CLIENT);
        Map<String, JsonObject> values = new TreeMap<>(local);
        if (server != null) {
            server.forEach((file, json) -> values.put(file, json.deepCopy()));
            for (ConfigOption option : edit(local).options()) {
                JsonElement mine = ConfigPaths.get(local.get(option.file()), option.path());
                if (option.side() == Side.CLIENT && mine != null && values.containsKey(option.file())) {
                    ConfigPaths.set(values.get(option.file()), option.path(), mine.deepCopy());
                }
            }
        }
        edit = edit(values);
        pending.forEach((file, changes) -> changes.forEach((path, value) -> {
            ConfigOption option = ConfigOptions.find(edit.options(), file, path);
            if (option != null) {
                edit.set(option, value);
            }
        }));
        if (initialized) {
            rebuildWidgets();
        }
    }

    private void say(Component text, int color) {
        message = text;
        messageColor = color;
    }

    private boolean serverShown() {
        return access != LOADING && access != UNAVAILABLE;
    }

    private boolean shown(ConfigOption option) {
        return option.side() == Side.CLIENT || serverShown();
    }

    private boolean editable(ConfigOption option) {
        return option.side() == Side.CLIENT || access == LOCAL || access == ConfigSync.OPERATOR || access == ConfigSync.HOST;
    }

    private List<String> groups() {
        List<String> groups = new ArrayList<>();
        for (String g : ConfigOptions.GROUPS) {
            if (edit.options().stream().anyMatch(o -> o.group().equals(g) && shown(o))) {
                groups.add(g);
            }
        }
        return groups;
    }

    private void cycle(int step) {
        List<String> groups = groups();
        int index = groups.indexOf(group);
        group = groups.get(Math.floorMod(index + step, groups.size()));
        rebuildWidgets();
    }

    @Override
    protected void init() {
        initialized = true;
        List<String> groups = groups();
        if (!groups.contains(group)) {
            group = groups.get(0);
        }
        int center = width / 2;
        addRenderableWidget(Button.builder(Component.literal("<"), b -> cycle(-1)).bounds(center - 130, 30, 20, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("config.remnant_mobs.section",
                Component.translatable("config.remnant_mobs.group." + group)), b -> cycle(1)).bounds(center - 106, 30, 212, 20).build());
        addRenderableWidget(Button.builder(Component.literal(">"), b -> cycle(1)).bounds(center + 110, 30, 20, 20).build());

        list = addRenderableWidget(new OptionList(minecraft, width, height - 56 - 44, 56));
        String file = null;
        String entry = null;
        for (ConfigOption option : edit.options()) {
            if (!option.group().equals(group) || !shown(option)) {
                continue;
            }
            if (!option.file().equals(file)) {
                file = option.file();
                entry = null;
                list.add(new HeaderRow(Component.translatable("config.remnant_mobs.file." + stem(file)), GOLD));
            }
            if (option.entry() != null && !option.entry().equals(entry) && entrySize(option) > 1) {
                entry = option.entry();
                list.add(new HeaderRow(Component.literal("◆ ").append(entryName(option)), TEXT));
            }
            list.add(new OptionRow(option));
        }

        save = addRenderableWidget(Button.builder(Component.translatable("config.remnant_mobs.save"), b -> save())
                .bounds(center - 154, height - 26, 150, 20).build());
        done = addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, b -> onClose()).bounds(center + 4, height - 26, 150, 20).build());
        refresh();
    }

    private int entrySize(ConfigOption option) {
        int count = 0;
        for (ConfigOption other : edit.options()) {
            if (other.group().equals(option.group()) && other.file().equals(option.file()) && option.entry().equals(other.entry())) {
                count++;
            }
        }
        return count;
    }

    private static String stem(String file) {
        return file.substring(0, file.length() - ".json".length());
    }

    private static Component entryName(ConfigOption option) {
        String entry = option.entry() == null ? "" : option.entry();
        if (option.docKey().contains("<factor>")) {
            return Component.translatable("config.remnant_mobs.factor." + entry);
        }
        if (option.docKey().contains("<rank>")) {
            return Component.translatable("rank.remnant_mobs." + entry);
        }
        if (option.docKey().contains("<trait>")) {
            return Component.translatable("trait.remnant_mobs." + entry);
        }
        return Component.literal(entry);
    }

    private Component label(ConfigOption option) {
        MutableComponent name = Component.translatable(option.translationKey());
        if (option.entry() != null && entrySize(option) == 1) {
            name.append(Component.literal(": ").withStyle(ChatFormatting.DARK_GRAY)).append(entryName(option));
        }
        return name;
    }

    private Component status() {
        return Component.translatable(switch (access) {
            case LOCAL -> "config.remnant_mobs.status.local";
            case LOADING -> "config.remnant_mobs.status.loading";
            case UNAVAILABLE -> "config.remnant_mobs.status.unavailable";
            case ConfigSync.HOST -> "config.remnant_mobs.status.host";
            case ConfigSync.OPERATOR -> "config.remnant_mobs.status.operator";
            default -> "config.remnant_mobs.status.read_only";
        });
    }

    /** Values, reset buttons and the Save button follow the edit after every change. */
    private void refresh() {
        if (list != null) {
            for (Row row : list.children()) {
                if (row instanceof OptionRow optionRow) {
                    optionRow.sync();
                }
            }
        }
        boolean dirty = savable();
        if (save != null) {
            save.active = dirty;
        }
        if (done != null) {
            done.setMessage(dirty ? CommonComponents.GUI_CANCEL : CommonComponents.GUI_DONE);
        }
    }

    private boolean savable() {
        return !edit.changes(Side.CLIENT).isEmpty() || (!edit.changes(Side.SERVER).isEmpty() && serverShown()
                && (access == LOCAL || access == ConfigSync.OPERATOR || access == ConfigSync.HOST));
    }

    private void save() {
        Map<String, Map<String, JsonElement>> mine = edit.changes(Side.CLIENT);
        Map<String, Map<String, JsonElement>> theirs = edit.changes(Side.SERVER);
        if (access == LOCAL) {
            applyHere(merge(mine, theirs), EnumSet.allOf(Side.class));
        } else if (access == ConfigSync.HOST) {
            // This computer runs the world: the integrated server writes and reloads, on its own thread.
            send(merge(mine, theirs));
        } else {
            if (!mine.isEmpty()) {
                applyHere(mine, EnumSet.of(Side.CLIENT));
            }
            if (!theirs.isEmpty() && access == ConfigSync.OPERATOR) {
                send(theirs);
            }
        }
        refresh();
    }

    private static Map<String, Map<String, JsonElement>> merge(Map<String, Map<String, JsonElement>> a, Map<String, Map<String, JsonElement>> b) {
        Map<String, Map<String, JsonElement>> out = new TreeMap<>();
        for (Map<String, Map<String, JsonElement>> part : List.of(a, b)) {
            part.forEach((file, values) -> out.computeIfAbsent(file, f -> new LinkedHashMap<>()).putAll(values));
        }
        return out;
    }

    /** Writes this computer's files and reloads them (no server runs in this game, or only own settings changed). */
    private void applyHere(Map<String, Map<String, JsonElement>> changes, Set<Side> sides) {
        try {
            ConfigFiles.Result result = ConfigFiles.apply(RemnantsConfig.dir(), changes, sides);
            RemnantsConfig.load(RemnantMobs.platform().configDir());
            if (result.refused().isEmpty()) {
                say(Component.translatable("config.remnant_mobs.saved", result.written()), GREEN);
            } else {
                say(Component.translatable("config.remnant_mobs.refused", result.refused().size(), String.join(", ", result.refused())), RED);
            }
        } catch (IOException | RuntimeException e) {
            RemnantMobs.LOG.error("Could not save Remnants settings", e);
            say(Component.translatable("config.remnant_mobs.failed", String.valueOf(e.getMessage())), RED);
        }
        Map<String, Map<String, JsonElement>> pendingServer = edit.changes(Side.SERVER);
        local = ConfigFiles.read(RemnantsConfig.dir());
        edit = edit(local);
        rebuildEdit();
        if (!sides.contains(Side.SERVER)) {
            pendingServer.forEach((file, values) -> values.forEach((path, value) -> {
                ConfigOption option = ConfigOptions.find(edit.options(), file, path);
                if (option != null) {
                    edit.set(option, value);
                }
            }));
        }
        rebuildWidgets();
    }

    /** Sends the changes to the server; its answer rebuilds the screen. */
    private void send(Map<String, Map<String, JsonElement>> changes) {
        if (!ConfigClient.canSend(ConfigUpdatePayload.TYPE)) {
            say(Component.translatable("config.remnant_mobs.status.unavailable"), RED);
            return;
        }
        String json = ConfigSync.encode(changes);
        if (json.length() > ConfigUpdatePayload.MAX_LENGTH) {
            say(Component.translatable("config.remnant_mobs.too_many"), RED);
            return;
        }
        ConfigClient.send(new ConfigUpdatePayload(json));
        say(Component.translatable("config.remnant_mobs.status.loading"), MUTED);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        super.extractRenderState(graphics, mouseX, mouseY, a);
        graphics.centeredText(font, title, width / 2, 6, TEXT);
        List<FormattedCharSequence> lines = font.split(status(), width - 40);
        if (!lines.isEmpty()) {
            graphics.centeredText(font, lines.get(0), width / 2, 18, MUTED);
        }
        Component bottom = message;
        if (bottom.getString().isEmpty() && savable()) {
            int count = edit.changes(Side.CLIENT).values().stream().mapToInt(Map::size).sum()
                    + edit.changes(Side.SERVER).values().stream().mapToInt(Map::size).sum();
            bottom = Component.translatable("config.remnant_mobs.unsaved", count);
        }
        if (!bottom.getString().isEmpty()) {
            List<FormattedCharSequence> text = font.split(bottom, width - 40);
            graphics.centeredText(font, text.get(0), width / 2, height - 38, bottom == message ? messageColor : GOLD);
        }
    }

    @Override
    public void onClose() {
        minecraft.setScreen(parent);
    }

    /** The tooltip of an option: what the docs say, whose setting it is, its default, range and key. */
    private List<Component> tooltip(ConfigOption option) {
        List<Component> lines = new ArrayList<>();
        lines.add(label(option).copy().withStyle(style -> style.withColor(GOLD)));
        lines.add(Component.translatable(option.translationKey() + ".tooltip"));
        MutableComponent side = Component.translatable(option.side() == Side.CLIENT ? "config.remnant_mobs.side.client" : "config.remnant_mobs.side.server");
        if (!editable(option)) {
            side.append(" · ").append(Component.translatable("config.remnant_mobs.read_only"));
        }
        lines.add(side.withStyle(ChatFormatting.GRAY));
        JsonElement def = option.defaultValue();
        if (def != null) {
            lines.add(Component.translatable("config.remnant_mobs.default", display(option, def)).withStyle(ChatFormatting.GRAY));
        }
        if (option.kind() == Kind.CHOICE) {
            lines.add(Component.translatable("config.remnant_mobs.choices", String.join(", ", option.choices())).withStyle(ChatFormatting.GRAY));
        } else if (option.kind() != Kind.TOGGLE) {
            boolean low = Double.isFinite(option.min());
            boolean high = Double.isFinite(option.max());
            String min = low ? ConfigOption.text(new JsonPrimitive(option.min())) : "";
            String max = high ? ConfigOption.text(new JsonPrimitive(option.max())) : "";
            if (low && high) {
                lines.add(Component.translatable("config.remnant_mobs.range", min, max).withStyle(ChatFormatting.GRAY));
            } else if (low) {
                lines.add(Component.translatable("config.remnant_mobs.range.min", min).withStyle(ChatFormatting.GRAY));
            } else if (high) {
                lines.add(Component.translatable("config.remnant_mobs.range.max", max).withStyle(ChatFormatting.GRAY));
            }
        }
        lines.add(Component.translatable("config.remnant_mobs.where", option.file(), option.path()).withStyle(ChatFormatting.DARK_GRAY));
        return lines;
    }

    private static Component tooltipText(List<Component> lines) {
        MutableComponent text = Component.empty();
        for (int i = 0; i < lines.size(); i++) {
            if (i > 0) {
                text.append("\n");
            }
            text.append(lines.get(i));
        }
        return text;
    }

    private static Component display(ConfigOption option, @Nullable JsonElement value) {
        if (option.kind() == Kind.TOGGLE) {
            return Component.translatable(value != null && value.getAsBoolean() ? "config.remnant_mobs.on" : "config.remnant_mobs.off");
        }
        return Component.literal(ConfigOption.text(value));
    }

    private final class OptionList extends ContainerObjectSelectionList<Row> {
        OptionList(Minecraft minecraft, int width, int height, int y) {
            super(minecraft, width, height, y, ROW_HEIGHT);
            this.centerListVertically = false;
        }

        void add(Row row) {
            addEntry(row);
        }

        @Override
        public int getRowWidth() {
            return Math.min(380, width - 40);
        }
    }

    private abstract static class Row extends ContainerObjectSelectionList.Entry<Row> {
    }

    private final class HeaderRow extends Row {
        private final Component text;
        private final int color;

        HeaderRow(Component text, int color) {
            this.text = text;
            this.color = color;
        }

        @Override
        public void extractContent(GuiGraphicsExtractor graphics, int mouseX, int mouseY, boolean hovered, float a) {
            graphics.text(font, text, getContentX(), getContentY() + (ROW_HEIGHT - 9) / 2 + 2, color, true);
        }

        @Override
        public List<? extends GuiEventListener> children() {
            return List.of();
        }

        @Override
        public List<? extends NarratableEntry> narratables() {
            return List.of();
        }
    }

    private final class OptionRow extends Row {
        private final ConfigOption option;
        private final AbstractWidget control;
        private final Button reset;
        private final Component label;
        private final List<Component> tooltip;

        OptionRow(ConfigOption option) {
            this.option = option;
            this.label = label(option);
            this.tooltip = tooltip(option);
            if (option.kind() == Kind.TOGGLE || option.kind() == Kind.CHOICE) {
                control = Button.builder(Component.empty(), b -> {
                    JsonElement current = edit.value(option);
                    if (option.kind() == Kind.TOGGLE) {
                        edit.set(option, new JsonPrimitive(current == null || !current.getAsBoolean()));
                    } else {
                        int index = current == null ? -1 : option.choices().indexOf(current.getAsString());
                        edit.set(option, new JsonPrimitive(option.choices().get((index + 1) % option.choices().size())));
                    }
                    say(Component.empty(), MUTED);
                    refresh();
                }).width(CONTROL_WIDTH).build();
            } else if (option.bounded()) {
                control = new ValueSlider(option);
            } else {
                EditBox box = new EditBox(font, 0, 0, CONTROL_WIDTH, 20, label);
                box.setMaxLength(32);
                box.setValue(ConfigOption.text(edit.value(option)));
                box.setResponder(text -> {
                    JsonElement parsed = option.parse(text, p -> Double.NaN);
                    box.setTextColor(parsed != null ? 0xFFE0E0E0 : RED);
                    if (parsed != null && edit.set(option, parsed)) {
                        say(Component.empty(), MUTED);
                        refreshOthers(this);
                    }
                });
                control = box;
            }
            control.setTooltip(Tooltip.create(tooltipText(tooltip)));
            reset = Button.builder(Component.literal("↺"), b -> {
                edit.reset(option);
                say(Component.empty(), MUTED);
                refresh();
            }).width(20).build();
            JsonElement def = option.defaultValue();
            reset.setTooltip(Tooltip.create(def == null ? Component.translatable("config.remnant_mobs.reset.none")
                    : Component.translatable("config.remnant_mobs.reset", display(option, def))));
            boolean canEdit = editable(option);
            control.active = canEdit;
            if (control instanceof EditBox box) {
                box.setEditable(canEdit);
            }
        }

        /** Shows the edit's value (after a reset, or when another option moved this one). */
        void sync() {
            JsonElement value = edit.value(option);
            if (control instanceof ValueSlider slider) {
                slider.sync();
            } else if (control instanceof EditBox box) {
                JsonElement typed = option.parse(box.getValue(), p -> Double.NaN);
                if (typed == null || !typed.equals(value)) {
                    box.setValue(ConfigOption.text(value));
                }
            } else {
                control.setMessage(display(option, value));
            }
            reset.active = editable(option) && option.defaultValue() != null && !edit.isDefault(option);
        }

        @Override
        public void extractContent(GuiGraphicsExtractor graphics, int mouseX, int mouseY, boolean hovered, float a) {
            int x = getContentX();
            int y = getContentY();
            int right = x + getContentWidth();
            control.setPosition(right - 24 - CONTROL_WIDTH, y);
            control.extractRenderState(graphics, mouseX, mouseY, a);
            reset.setPosition(right - 20, y);
            reset.extractRenderState(graphics, mouseX, mouseY, a);
            int labelWidth = right - 30 - CONTROL_WIDTH - x;
            int color = !editable(option) ? MUTED : edit.changed(option) ? GOLD : TEXT;
            List<FormattedCharSequence> text = font.split(label, labelWidth);
            graphics.text(font, text.isEmpty() ? FormattedCharSequence.EMPTY : text.get(0), x, y + 6, color, true);
            if (hovered && mouseX < x + labelWidth && mouseY >= y && mouseY < y + 20) {
                List<FormattedCharSequence> lines = new ArrayList<>();
                for (Component line : tooltip) {
                    lines.addAll(font.split(line, 260));
                }
                graphics.setTooltipForNextFrame(lines, mouseX, mouseY);
            }
        }

        @Override
        public List<? extends GuiEventListener> children() {
            return List.of(control, reset);
        }

        @Override
        public List<? extends NarratableEntry> narratables() {
            return List.of(control, reset);
        }
    }

    /** After typing into a field, refresh every row but that field (so the cursor stays put). */
    private void refreshOthers(OptionRow typing) {
        if (list != null) {
            for (Row row : list.children()) {
                if (row instanceof OptionRow optionRow && optionRow != typing) {
                    optionRow.sync();
                }
            }
            typing.reset.active = editable(typing.option) && typing.option.defaultValue() != null && !edit.isDefault(typing.option);
        }
        boolean dirty = savable();
        if (save != null) {
            save.active = dirty;
        }
        if (done != null) {
            done.setMessage(dirty ? CommonComponents.GUI_CANCEL : CommonComponents.GUI_DONE);
        }
    }

    /** A slider for an option with both ends set; integers move in whole steps, numbers in round steps. */
    private final class ValueSlider extends AbstractSliderButton {
        private final ConfigOption option;
        private final double step;

        ValueSlider(ConfigOption option) {
            super(0, 0, CONTROL_WIDTH, 20, Component.empty(), 0);
            this.option = option;
            this.step = option.kind() == Kind.INTEGER ? 1 : niceStep((option.max() - option.min()) / 100);
            sync();
        }

        void sync() {
            JsonElement current = edit.value(option);
            double v = current == null ? option.min() : current.getAsDouble();
            value = Math.max(0, Math.min(1, (v - option.min()) / (option.max() - option.min())));
            updateMessage();
        }

        @Override
        protected void updateMessage() {
            setMessage(Component.literal(ConfigOption.text(edit.value(option))));
        }

        @Override
        protected void applyValue() {
            double raw = option.min() + value * (option.max() - option.min());
            double snapped = option.min() + Math.round((raw - option.min()) / step) * step;
            snapped = Math.round(snapped * 1e6) / 1e6;
            edit.set(option, new JsonPrimitive(snapped));
            say(Component.empty(), MUTED);
            refreshOthersFromSlider(this);
        }
    }

    private void refreshOthersFromSlider(ValueSlider moving) {
        if (list != null) {
            for (Row row : list.children()) {
                if (row instanceof OptionRow optionRow) {
                    if (optionRow.control == moving) {
                        moving.updateMessage();
                        optionRow.reset.active = editable(optionRow.option) && optionRow.option.defaultValue() != null
                                && !edit.isDefault(optionRow.option);
                    } else {
                        optionRow.sync();
                    }
                }
            }
        }
        boolean dirty = savable();
        if (save != null) {
            save.active = dirty;
        }
        if (done != null) {
            done.setMessage(dirty ? CommonComponents.GUI_CANCEL : CommonComponents.GUI_DONE);
        }
    }

    /** 1, 2 or 5 times a power of ten, near {@code raw}. */
    static double niceStep(double raw) {
        if (!(raw > 0)) {
            return 0.01;
        }
        double power = Math.pow(10, Math.floor(Math.log10(raw)));
        double mantissa = raw / power;
        double nice = mantissa < 1.5 ? 1 : mantissa < 3.5 ? 2 : mantissa < 7.5 ? 5 : 10;
        return nice * power;
    }
}
