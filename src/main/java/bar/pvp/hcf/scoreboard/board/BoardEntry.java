package bar.pvp.hcf.scoreboard.board;

import bar.pvp.hcf.utils.CC;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

@Getter
@Setter
public class BoardEntry {

    private Board board;
    private String text;
    private String textOriginal;
    private String key;
    private Team team;

    public BoardEntry(Board board, String text) {
        this.text = text;
        this.textOriginal = text;
        this.board = board;
        this.key = board.getNewKey(this);

        setup();
    }

    public void setup() {
        if(Bukkit.getPluginManager().getPlugin(board.getQuery().getPlugin().getName()) == null) { this.remove(); return; }

        Scoreboard scoreboard = board.getScoreboard();
        text = CC.translate(this.text);
        String teamName = key;

        if(teamName.length() > 16) teamName = teamName.substring(0, 16);

        if(scoreboard.getTeam(teamName) != null) team = scoreboard.getTeam(teamName);
        else {
            team = scoreboard.registerNewTeam(teamName);
        }

        if(!team.getEntries().contains(this.key)) team.addEntry(teamName);
        if(!board.getEntries().contains(this)) this.board.getEntries().add(this);
    }

    public void send(int position) {
        if (Bukkit.getPluginManager().getPlugin(board.getQuery().getPlugin().getName()) == null) {
            this.remove();
            return;
        }
        Objective objective = this.board.getObjective();
        if (this.text.length() > 16) {
            boolean fix = this.text.toCharArray()[15] == '§';
            String prefix = fix ? this.text.substring(0, 15) : this.text.substring(0, 16);
            String suffix = fix ? this.text.substring(15, this.text.toCharArray().length) : (CC.getLastColors(prefix) + this.text.substring(16, this.text.toCharArray().length));
            this.team.setPrefix(prefix);
            if (suffix.length() > 16) {
                this.team.setSuffix(suffix.substring(0, 16));
            } else {
                this.team.setSuffix(suffix);
            }
        } else {
            this.team.setPrefix(this.text);
            this.team.setSuffix("");
        }
        Score score = objective.getScore(this.key);
        score.setScore(position);
    }

    public void remove() {
        if(board.getKeys().contains(this.key)) {
            board.getKeys().remove(this.key);
            board.getScoreboard().resetScores(this.key);
        }
    }

}
