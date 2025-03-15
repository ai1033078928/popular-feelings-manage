package com.hrbu;

import com.hankcs.hanlp.HanLP;
import com.hankcs.hanlp.classification.classifiers.IClassifier;
import com.hankcs.hanlp.classification.classifiers.NaiveBayesClassifier;
import com.hankcs.hanlp.classification.models.AbstractModel;
import com.hankcs.hanlp.corpus.io.IOUtil;
import com.hrbu.utility.TestUtility;

import java.io.File;
import java.io.IOException;

/**
 * 第一个demo,演示文本分类最基本的调用方式
 */
public class HaNLP_Test {
    /**
     * 中文情感挖掘语料-ChnSentiCorp 谭松波
     */
    public static final String CORPUS_FOLDER = TestUtility.ensureTestData("ChnSentiCorp情感分析酒店评论", "http://file.hankcs.com/corpus/ChnSentiCorp.zip");
    //public static final String CORPUS_FOLDER = TestUtility.ensureTestData("微博评论", "");
    public static final String MODEL_PATH = "D:\\Program Files\\JetBrains\\ideaProjects\\popular-feelings-manage\\data\\myModel.ser";

    public static void main(String[] args) throws IOException
    {
        IClassifier classifier = new NaiveBayesClassifier(); // 创建分类器，更高级的功能请参考IClassifier的接口定义
        classifier.train(CORPUS_FOLDER);                     // 训练后的模型支持持久化，下次就不必训练了
        AbstractModel model = classifier.getModel();
        IOUtil.saveObjectTo(model, MODEL_PATH);

//        predict(classifier, "前台客房服务态度非常好！早餐很丰富，房价很干净。再接再厉！");
//        predict(classifier, "结果大失所望，灯光昏暗，空间极其狭小，床垫质量恶劣，房间还伴着一股霉味。");
//        predict(classifier, "可利用文本分类实现情感分析，效果还行");
        predict(classifier, "致敬南仁东！");
        predict(classifier, "民族偶像");
        predict(classifier, "看不懂说的啥");
        predict(classifier, "每个女孩，都有自己的了不起。你的优秀，不需要任何人来证明。因为女人最大的精彩，就是独立。");
        predict(classifier, "真你妈恶心");
        predict(classifier, "精英都揣着明白装糊涂，掩盖阶级矛盾");
        predict(classifier, "不过革命会流血牺牲，你们准备好了么");
        predict(classifier, "再急·也要注意语气·再苦·也别忘记坚持·再累·也要爱惜自己");
        predict(classifier, "坐标河南 已经是快不行了…");
        predict(classifier, "这个博物馆不一般，宝贝很多，一点点拿出来……我大概能看到七老八十的！");
        predict(classifier, "看着舒服\uD83D\uDE0C");
        predict(classifier, "不让用地膜，你倒是告诉农民用什么啊");
    }

    private static void predict(IClassifier classifier, String text)
    {
        System.out.printf("《%s》 情感极性是 【%s】\n", text, classifier.classify(text));
    }

    static
    {
        File corpusFolder = new File(CORPUS_FOLDER);
        if (!corpusFolder.exists() || !corpusFolder.isDirectory())
        {
            System.err.println("没有文本分类语料，请阅读IClassifier.train(java.lang.String)中定义的语料格式、准备语料");
            System.exit(1);
        }
    }
}
