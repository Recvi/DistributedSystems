

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by stathis on 3/17/16.
 */
public class Paragraph {

    List<Line> lines;


    public Paragraph(List<Line> lines) {
        this.lines = lines;
    }

    public List<Line> getLines() {
        return lines;
    }

    public void setLines(List<Line> lines) {
        this.lines = lines;
    }


    public int count(String text){
        int count=0;
        for (Line line : lines) {

            count+=line.count(text);
        }
        return count;
    }

    public boolean contains(String test) {
      Stream<Line> result=   lines.parallelStream().filter(p -> p.getLine()
              .contains(test));
        this.lines = result.collect(Collectors.toList());
        System.out.println(lines.size());
    if (lines.size()>0)
        return true;
        else
        return false;
    }
}