import com.hankcs.hanlp.HanLP;
import com.hankcs.hanlp.model.perceptron.PerceptronLexicalAnalyzer;
import com.hankcs.hanlp.seg.common.Term;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class FenCiTest {
    public static void main(String[] args) {
        //需要过滤的字符
        String filterStr = "`~!@#$^&*()=|{}':;',\\[\\].<>/?~！@#￥……&*（）——|{}【】‘；：”“'。，、？ ";
        //计算分词
        List<Term> termList = HanLP.segment("判断不存在则新增(去重)");
        //过滤一下字符
        List<String> list = termList.stream().map(a -> a.word).filter(s -> !filterStr.contains(s)).collect(Collectors.toList());




        System.out.println(list);
        System.out.println(HanLP.segment("判断不存在则新增(去重)").toString());


        /*try {
            PerceptronLexicalAnalyzer analyzer = new PerceptronLexicalAnalyzer(
                    "data/model/perceptron/pku199801/cws.bin",
                    HanLP.Config.PerceptronPOSModelPath,
                    HanLP.Config.PerceptronNERModelPath);

            System.out.println(analyzer.analyze("判断不存在则新增(去重)"));

        } catch (IOException e) {
            e.printStackTrace();
        }*/
    }
}

