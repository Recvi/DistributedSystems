
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MapReduce {

    /**
     * @param args
     */
    public static void main(String[] args) {
        double counted;
//       List<Line> lines = Arrays.asList(new Line("first test"), new Line("map test"), new Line("test test"),
//               new Line("map reduce"));
//
//       lines.forEach(System.out::println);
//
//       counted = lines.stream().parallel().filter(p -> p.getLine().contains("test"))
//               .map(p -> p.count("test")).reduce((sum, p) -> sum + p).get();
//
//       System.out.println(counted);

        List<Paragraph> paragraphs = new ArrayList<>();

        boolean add = paragraphs.add(new Paragraph(Arrays.asList(new Line("first test"),
                new Line("map test"), new Line("test test"),
                new Line("map reduce"))));

        add = paragraphs.add(new Paragraph(Arrays.asList(new Line("first test"), new Line("map test"),
                new Line("map reduce"))));

        counted = paragraphs.stream().parallel().filter(p -> p.contains("test"))
                .map(p -> p.count("test")).reduce((sum, p) -> {
//                    System.out.println("sum: "+ sum);
//                    System.out.println("p: "+ p);
                    return sum + p;}).get();

        System.out.println(counted);
    }


}