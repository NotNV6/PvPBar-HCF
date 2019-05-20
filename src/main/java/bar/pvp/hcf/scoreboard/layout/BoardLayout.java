package bar.pvp.hcf.scoreboard.layout;

import bar.pvp.hcf.utils.CC;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class BoardLayout {

    private String title;
    private List<String> strings;
    @Setter private BoardLayout layout;
    private static List<BoardLayout> layouts = new ArrayList<>();

    public BoardLayout() {
        strings  = new ArrayList<>();

        layouts.add(this);
    }

    public void setTitle(String title) { this.title = title; }
    public void add(String string) { strings.add(CC.translate(string)); }
}
