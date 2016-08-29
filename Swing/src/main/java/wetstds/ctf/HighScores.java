package wetstds.ctf;

import java.io.Serializable;
import java.util.Map;
import java.util.stream.Stream;
import wetstds.ctf.data.Maps;

public class HighScores extends Object implements Serializable
{
    /**
    * 
    */
   private static final long serialVersionUID = 2473280273824798648L;

   private Map<Difficulty, HighScore> topTimes = Stream.of(
            Maps.entry(Difficulty.BEGINNER, new HighScore("Anonymous", 999)),
            Maps.entry(Difficulty.INTERMEDIATE, new HighScore("Anonymous", 999)),
            Maps.entry(Difficulty.EXPERT, new HighScore("Anonymous", 999))).
            collect(Maps.entriesToMap());

    public HighScore getScore(Difficulty difficulty)
    {
       return topTimes.get(difficulty);
    }

    public HighScore setHighScore(String name, Difficulty difficulty, int time)
    {
       HighScore result = new HighScore(name, time);
       topTimes.put(difficulty, result);
       return result;
    }

    public static class HighScore extends Object implements Serializable
    {
       /**
       * 
       */
      private static final long serialVersionUID = 7971434667184522665L;

       private int time;
       private String name;

       private HighScore(String name, int time)
       {
          this.name = name;
          this.time = time;
       }

       public String getName()
       {
          return name;
       }

       public int getTime()
       {
          return time;
       }
    }
}
