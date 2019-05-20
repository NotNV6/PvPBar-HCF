package bar.pvp.hcf.scoreboard.board;

import bar.pvp.hcf.scoreboard.Query;
import bar.pvp.hcf.scoreboard.layout.BoardLayout;
import bar.pvp.hcf.utils.CC;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
public class Board {

    @Getter  private static Set<Board> boards = new HashSet<>();
    private Scoreboard scoreboard;
    private Player player;
    private Objective objective;
    private Set<String> keys;
    private List<BoardEntry> entries;
    private Query query;

    public Board(Player player, Query query) {
        this.player = player;
        this.query = query;
        this.keys = new HashSet<>();
        this.entries = new ArrayList<>();

        setup();
    }

    private void setup() {
        if(!this.player.getScoreboard().equals(Bukkit.getScoreboardManager().getMainScoreboard())) this.scoreboard = this.player.getScoreboard();else this.scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        (this.objective = this.scoreboard.registerNewObjective("query", "dummy")).setDisplaySlot(DisplaySlot.valueOf("SIDEBAR"));
        BoardLayout layout = Query.getInstance().getLayout().getLayout(player);

        if(layout.getTitle() != null) this.objective.setDisplayName(CC.translate(layout.getTitle())); else this.objective.setDisplayName(CC.translate("&cTitle not set"));

        boards.add(this);
    }

    String getNewKey(BoardEntry entry) {
        CC[] values;
        for (int length = (values = CC.values()).length, i = 0; i < length; ++i) {
            CC color = values[i];
            String colorText = color.toString() + CC.WHITE;
            if (entry.getText().length() > 16) {
                String sub = entry.getText().substring(0, 16);
                colorText = colorText + CC.getLastColors(sub);
            }
            if (!this.keys.contains(colorText)) {
                this.keys.add(colorText);
                return colorText;
            }
        }
        throw new IndexOutOfBoundsException("No more keys.");
    }

    public List<String> getBoardEntriesFormatted() {
        List<String> toReturn = new ArrayList<>();
        new ArrayList<>(this.entries).forEach(entry -> toReturn.add(entry.getText()));

        return toReturn;
    }

    public BoardEntry getByPosition(int position) {
        int i = 0;
        for (BoardEntry board : this.entries) {
            if (i == position) {
                return board;
            }
            ++i;
        }
        return null;
    }

    public static Board getByPlayer(Player player) { return boards.stream().filter(board -> board.getPlayer().equals(player)).findFirst().orElse(null); }

}
