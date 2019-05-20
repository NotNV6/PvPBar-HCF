package bar.pvp.hcf.scoreboard;

import bar.pvp.hcf.scoreboard.board.Board;
import bar.pvp.hcf.scoreboard.board.BoardEntry;
import bar.pvp.hcf.scoreboard.layout.BoardLayout;
import bar.pvp.hcf.scoreboard.layout.LayoutProvider;
import bar.pvp.hcf.HCTeams;
import bar.pvp.hcf.utils.CC;
import bar.pvp.hcf.utils.Config;
import bar.pvp.hcf.utils.player.PlayerUtil;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class QueryTask extends BukkitRunnable implements Listener {

    public void run() {
        if (Query.getInstance().getLayout() == null) return;

        PlayerUtil.getOnlinePlayers().forEach(player -> {
            LayoutProvider provider = Query.getInstance().getLayout();

            if(provider == null) return;
            BoardLayout layout = provider.getLayout(player);
            if(layout == null) return;

            List<String> strings = layout.getStrings();
            List<String> translatedStrings = new ArrayList<>();
            if(strings.isEmpty()) return;

            Board board = Board.getByPlayer(player);
            if(board == null) return;

            strings.forEach(string -> translatedStrings.add(CC.translate(string)));
            Collections.reverse(strings);

            Scoreboard scoreboard = board.getScoreboard();
            Objective objective = board.getObjective();
            if (!objective.getDisplayName().equalsIgnoreCase(layout.getTitle())) objective.setDisplayName(CC.translate(layout.getTitle()));

            for(int i = 0; i < strings.size(); i++) {
                String text = strings.get(i);
                int pos;

                pos = i + 1;

                Iterator<BoardEntry> iterator = board.getEntries().iterator();
                while (iterator.hasNext()) {
                    BoardEntry entry = iterator.next();
                    int entryPosition = scoreboard.getObjective(DisplaySlot.SIDEBAR).getScore(entry.getKey()).getScore();
                    if (entryPosition > strings.size()) {
                        iterator.remove();
                        entry.remove();
                    }
                }

                int positionToSearch;
                positionToSearch = pos - 1;

                BoardEntry entry = board.getByPosition(positionToSearch);

                if (entry == null) new BoardEntry(board, text).send(pos);
                else {
                    entry.setText(text);
                    entry.setup();
                    entry.send(pos);
                }


                if (board.getEntries().size() > strings.size()) {
                    iterator = board.getEntries().iterator();
                    while (iterator.hasNext()) {
                        entry = iterator.next();
                        if (!translatedStrings.contains(entry.getText()) || Collections.frequency(board.getBoardEntriesFormatted(), entry.getText()) > 1) {
                            iterator.remove();
                            entry.remove();
                        }
                    }
                }


            }
            
            
            Config c = HCTeams.getInstance().getScoreboardConfig();
            int minLineSize = c.getStringList("SCOREBOARD.HEADER").size() + c.getStringList("SCOREBOARD.FOOTER").size();

            if(scoreboard.getEntries().size() <= minLineSize && !HCTeams.getInstance().isKitMap()) return;

            player.setScoreboard(scoreboard);
        });
    }


    public static void setupBoard(PlayerJoinEvent event) {
        if (Board.getByPlayer(event.getPlayer()) == null) new Board(event.getPlayer(), Query.getInstance());

    }

    @SuppressWarnings("unused")
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerQuitEvent(PlayerQuitEvent event) {
        Board board = Board.getByPlayer(event.getPlayer());
        if (board != null) Board.getBoards().remove(board);
    }

}
