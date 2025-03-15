import com.hankcs.hanlp.HanLP;
import com.hankcs.hanlp.mining.cluster.ClusterAnalyzer;
import com.hankcs.hanlp.seg.common.Term;

import java.util.List;
import java.util.stream.Collectors;

//https://github.com/hankcs/HanLP/wiki/%E6%96%87%E6%9C%AC%E8%81%9A%E7%B1%BB
public class ClusterAnalyzerTest {
    public static void main(String[] args) {

        /*
        ClusterAnalyzer<String> analyzer = new ClusterAnalyzer<String>();
        analyzer.addDocument("赵一", HanLP.segment("在叛军谣言四起的时候，准备部署联合军，这是第一要点，相信各位都知道").toString());
        analyzer.addDocument("钱二", HanLP.segment("打人者发文：打击骗子剧组，希望各位女演员结身自好，水泊梁山众兄弟管了！总有人说贵圈很乱。其实乱的就是这样的骗子").toString());
        analyzer.addDocument("张三", HanLP.segment("我看着她的眼睛，笑了一笑，想起第一次见她的样子。").toString());
        analyzer.addDocument("李四", HanLP.segment("在叛军出现初期，交战的重点应当是压制叛军，建议点上空袭来配合联合军将叛军死死压制").toString());
        analyzer.addDocument("王五", HanLP.segment("初期看情况，兵力不足补联合军，一般三支就够，少的话两支，如果一支够用那就是纯粹是叛军给面子（运气好）").toString());
        analyzer.addDocument("马六", HanLP.segment("无人侦察机配合空袭炸营地能够很好地压制叛军").toString());
        analyzer.addDocument("七", HanLP.segment("一念成佛，一念成魔。").toString());
        analyzer.addDocument("八", HanLP.segment("指如削葱根，口如含朱丹。纤纤作细步，精妙世无双。").toString());


        //对比两种算法，repeated bisection不仅准确率比kmeans更高，而且速度是kmeans的三倍。然而repeated bisection成绩波动较大，需要多运行几次才可能得出这样的结果。也许85%左右的准确率并不好看，但考虑到聚类是一种无监督学习，其性价比依然非常可观
        System.out.println(analyzer.kmeans(3));

        System.out.println("##################################################################");
        System.out.println(analyzer.repeatedBisection(3));
        */

    }
}
