package com.qurve.level.service;

import com.qurve.global.enums.ErrorCode;
import com.qurve.global.enums.LearningLanguage;
import com.qurve.global.exception.BusinessException;
import com.qurve.level.dto.request.LevelTestRequestDto;
import com.qurve.level.dto.request.LevelTestResultRequestDto;
import com.qurve.level.dto.request.SaveLevelRequestDto;
import com.qurve.level.dto.response.*;
import com.qurve.user.domain.User;
import com.qurve.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LevelService {

    private final UserRepository userRepository;

    /**
     * 로그인한 사용자의 학습 언어에 맞는 사전 레벨 테스트 질문 조회
     *
     * 사용자의 학습 경험과 읽기·말하기 능력을 파악하기 위한
     * 사전 질문 목록을 반환한다.
     *
     * @param loginId 로그인 ID
     * @return 학습 언어에 맞는 사전 레벨 테스트 질문 목록
     * @throws BusinessException 사용자가 존재하지 않는 경우
     */
    public PreQuestionResponseDto getPreQuestions(String loginId) {
        User user = findUser(loginId);

        if (user.getLearningLanguage() == LearningLanguage.ENGLISH) {
            return getEnglishPreQuestions();
        }
        return getJapanesePreQuestions();
    }

    private PreQuestionResponseDto getJapanesePreQuestions() {
        List<OptionDto> option1 = List.of(
                new OptionDto(1, "처음 시작해요 (0개월)"),
                new OptionDto(2, "3개월 미만"),
                new OptionDto(3, "3개월 ~ 1년 미만"),
                new OptionDto(4, "1년 이상")
        );

        List<OptionDto> option2 = List.of(
                new OptionDto(1, "둘 다 읽을 수 있어요"),
                new OptionDto(2, "히라가나만 읽을 수 있어요"),
                new OptionDto(3, "아직 어려워요")
        );

        List<OptionDto> option3 = List.of(
                new OptionDto(1, "네, 간단한 회화가 가능해요"),
                new OptionDto(2, "짧은 문장, 단어만 말할 수 있어요"),
                new OptionDto(3, "아직 어려워요")
        );

        List<PreQuestionDto> questions = List.of(
                new PreQuestionDto(
                        1,
                        "일본어를 배워본 기간이 얼마나 되나요?",
                        option1
                ),
                new PreQuestionDto(
                        2,
                        "히라가나·가타카나를 읽을 수 있나요?",
                        option2
                ),
                new PreQuestionDto(
                        3,
                        "일본어로 말할 수 있나요?",
                        option3
                )
        );

        return new PreQuestionResponseDto(questions);
    }

    private PreQuestionResponseDto getEnglishPreQuestions() {
        List<OptionDto> option1 = List.of(
                new OptionDto(1, "학교 수업 외에는 따로 공부해 본 적이 거의 없어요"),
                new OptionDto(2, "기본 단어와 문법을 중심으로 공부했어요"),
                new OptionDto(3, "간단한 글을 읽고 문제를 풀 수 있을 정도로 공부했어요"),
                new OptionDto(4, "다양한 글을 이해하고 영어를 실제 상황에서 사용해 본 경험이 있어요")
        );

        List<OptionDto> option2 = List.of(
                new OptionDto(1, "일상적인 주제의 짧은 글은 전체 내용을 이해할 수 있어요"),
                new OptionDto(2, "익숙한 단어와 간단한 문장은 이해할 수 있어요"),
                new OptionDto(3, "아는 단어는 있지만 문장의 전체 의미를 이해하기 어려워요")
        );

        List<OptionDto> option3 = List.of(
                new OptionDto(1, "일상적인 주제로 간단한 대화를 이어갈 수 있어요"),
                new OptionDto(2, "자기소개나 익숙한 표현을 짧게 말할 수 있어요"),
                new OptionDto(3, "단어는 알고 있지만 문장으로 말하기는 어려워요")
        );

        List<PreQuestionDto> questions = List.of(
                new PreQuestionDto(
                        1,
                        "영어를 어느 정도 공부해 보셨나요?",
                        option1
                ),
                new PreQuestionDto(
                        2,
                        "영어로 된 짧은 글을 어느 정도 이해할 수 있나요?",
                        option2
                ),
                new PreQuestionDto(
                        3,
                        "영어로 어느 정도 말할 수 있나요?",
                        option3
                )
        );

        return new PreQuestionResponseDto(questions);
    }

    /**
     * 레벨 테스트 문항 조회
     *
     * * 사전 질문 응답을 기반으로 사용자의 예상 학습 수준을 판단한 뒤,
     * 해당 수준에 맞는 문제 세트를 반환한다.
     *
     * @param dto 사전 질문 응답 정보
     * @param loginId 로그인 ID
     * @return 사용자 수준에 맞는 레벨 테스트 문제 목록
     * @throws BusinessException 사용자가 존재하지 않는 경우
     */
    public LevelTestResponseDto getLevelTestQuestions(LevelTestRequestDto dto, String loginId) {
        User user = findUser(loginId);

        int caseNumber = determineCase(
                dto.getPre1Answer(),
                dto.getPre2Answer(),
                dto.getPre3Answer()
        );

        List<LevelTestQuestionDto> questions = getQuestionsByCase(caseNumber, user.getLearningLanguage());

        return new LevelTestResponseDto(questions);
    }

    /**
     * 사전 질문 응답 기반 문제 세트 결정
     *
     * * 학습 기간, 읽기 능력, 말하기 능력을 종합 평가하며
     * 점수가 높을수록 더 높은 난이도의 문제를 제공한다.
     *
     * @param pre1Answer 사전질문1 응답
     * @param pre2Answer 사전질문2 응답
     * @param pre3Answer 사전질문3 응답
     * @return 문제 세트 번호
     */
    private int determineCase(int pre1Answer, int pre2Answer, int pre3Answer) {
        int score = 0;

        score += pre1Answer - 1;

        if (pre2Answer == 1) score += 2;
        else if (pre2Answer == 2) score += 1;

        if (pre3Answer == 1) score += 2;
        else if (pre3Answer == 2) score += 1;

        if (score <= 2) return 1;
        if (score <= 4) return 2;
        return 3;
    }

    /**
     * 레벨 테스트 문항 조회
     *
     * * 사용자의 사전 질문 결과에 따라 서로 다른 난이도의
     * 문제 세트를 제공하기 위해 케이스별 문항을 분리하여 관리한다.
     *
     * @param caseNumber 문제 세트 번호
     * @return 해당 케이스의 레벨 테스트 문항 목록
     */
    private List<LevelTestQuestionDto> getQuestionsByCase(int caseNumber, LearningLanguage learningLanguage) {
        if (learningLanguage == LearningLanguage.ENGLISH) {
            return getEnglishQuestionsByCase(caseNumber);
        }

        return getJapaneseQuestionsByCase(caseNumber);
    }

    private List<LevelTestQuestionDto> getJapaneseQuestionsByCase(int caseNumber) {
        return switch (caseNumber) {
            case 1 -> getJapaneseCase1Questions();
            case 2 -> getJapaneseCase2Questions();
            default -> getJapaneseCase3Questions();
        };
    }

    private List<LevelTestQuestionDto> getEnglishQuestionsByCase(int caseNumber) {
        return switch (caseNumber) {
            case 1 -> getEnglishCase1Questions();
            case 2 -> getEnglishCase2Questions();
            default -> getEnglishCase3Questions();
        };
    }

    // Case 1 문제 데이터
    private List<LevelTestQuestionDto> getJapaneseCase1Questions() {
        return List.of(
                new LevelTestQuestionDto(1, "「みず」の 뜻은 무엇인가요?", "쉬움",
                        List.of(
                                new OptionDto(1, "불"),
                                new OptionDto(2, "물"),
                                new OptionDto(3, "나무"),
                                new OptionDto(4, "책"),
                                new OptionDto(5, "모르겠어요")
                        ), 2),
                new LevelTestQuestionDto(2, "「いぬ」は 무엇인가요?", "쉬움",
                        List.of(
                                new OptionDto(1, "고양이"),
                                new OptionDto(2, "새"),
                                new OptionDto(3, "개"),
                                new OptionDto(4, "물고기"),
                                new OptionDto(5, "모르겠어요")
                        ), 3),
                new LevelTestQuestionDto(3, "아침 인사로 알맞은 것은?", "쉬움",
                        List.of(
                                new OptionDto(1, "こんにちは"),
                                new OptionDto(2, "おはよう"),
                                new OptionDto(3, "さようなら"),
                                new OptionDto(4, "おやすみ"),
                                new OptionDto(5, "모르겠어요")
                        ), 2),
                new LevelTestQuestionDto(4, "「わたし（　）学生です。」에 들어갈 알맞은 조사는?", "쉬움",
                        List.of(
                                new OptionDto(1, "は"),
                                new OptionDto(2, "を"),
                                new OptionDto(3, "に"),
                                new OptionDto(4, "で"),
                                new OptionDto(5, "모르겠어요")
                        ), 1),
                new LevelTestQuestionDto(5, "「ほん」の 뜻은 무엇인가요?", "쉬움",
                        List.of(
                                new OptionDto(1, "연필"),
                                new OptionDto(2, "가방"),
                                new OptionDto(3, "책"),
                                new OptionDto(4, "공책"),
                                new OptionDto(5, "모르겠어요")
                        ), 3),
                new LevelTestQuestionDto(6, "「ねこ」는 무엇인가요?", "쉬움",
                        List.of(
                                new OptionDto(1, "개"),
                                new OptionDto(2, "고양이"),
                                new OptionDto(3, "토끼"),
                                new OptionDto(4, "말"),
                                new OptionDto(5, "모르겠어요")
                        ), 2),
                new LevelTestQuestionDto(7, "숫자 3은 일본어로 무엇인가요?", "쉬움",
                        List.of(
                                new OptionDto(1, "いち"),
                                new OptionDto(2, "に"),
                                new OptionDto(3, "さん"),
                                new OptionDto(4, "よん"),
                                new OptionDto(5, "모르겠어요")
                        ), 3),
                new LevelTestQuestionDto(8, "「ありがとうございます」의 뜻으로 가장 알맞은 것은?", "쉬움",
                        List.of(
                                new OptionDto(1, "미안합니다"),
                                new OptionDto(2, "반갑습니다"),
                                new OptionDto(3, "감사합니다"),
                                new OptionDto(4, "잘 자요"),
                                new OptionDto(5, "모르겠어요")
                        ), 3),
                new LevelTestQuestionDto(9, "「がっこう」의 뜻은 무엇인가요?", "쉬움",
                        List.of(
                                new OptionDto(1, "회사"),
                                new OptionDto(2, "집"),
                                new OptionDto(3, "학교"),
                                new OptionDto(4, "병원"),
                                new OptionDto(5, "모르겠어요")
                        ), 3),
                new LevelTestQuestionDto(10, "「パン（　）たべます。」에 들어갈 조사는?", "쉬움",
                        List.of(
                                new OptionDto(1, "が"),
                                new OptionDto(2, "を"),
                                new OptionDto(3, "に"),
                                new OptionDto(4, "と"),
                                new OptionDto(5, "모르겠어요")
                        ), 2)
        );
    }

    // Case 2 문제 데이터
    private List<LevelTestQuestionDto> getJapaneseCase2Questions() {
        return List.of(
                new LevelTestQuestionDto(1, "「まいにち 7じ（　）おきます。」에 들어갈 알맞은 것은?", "쉬움",
                        List.of(
                                new OptionDto(1, "を"),
                                new OptionDto(2, "に"),
                                new OptionDto(3, "が"),
                                new OptionDto(4, "で"),
                                new OptionDto(5, "모르겠어요")
                        ), 2),
                new LevelTestQuestionDto(2, "「きのう ともだち（　）えいがを みました。」", "쉬움",
                        List.of(
                                new OptionDto(1, "と"),
                                new OptionDto(2, "に"),
                                new OptionDto(3, "を"),
                                new OptionDto(4, "で"),
                                new OptionDto(5, "모르겠어요")
                        ), 1),
                new LevelTestQuestionDto(3, "「わたしは でんしゃで かいしゃ（　）いきます。」", "쉬움",
                        List.of(
                                new OptionDto(1, "は"),
                                new OptionDto(2, "を"),
                                new OptionDto(3, "に"),
                                new OptionDto(4, "が"),
                                new OptionDto(5, "모르겠어요")
                        ), 3),
                new LevelTestQuestionDto(4, "「あつい」の 반대말은 무엇인가요?", "쉬움",
                        List.of(
                                new OptionDto(1, "さむい"),
                                new OptionDto(2, "たかい"),
                                new OptionDto(3, "おそい"),
                                new OptionDto(4, "ちいさい"),
                                new OptionDto(5, "모르겠어요")
                        ), 1),
                new LevelTestQuestionDto(5, "「日曜日に うちで 本を（　）。」", "쉬움",
                        List.of(
                                new OptionDto(1, "よみます"),
                                new OptionDto(2, "みます"),
                                new OptionDto(3, "ききます"),
                                new OptionDto(4, "あいます"),
                                new OptionDto(5, "모르겠어요")
                        ), 1),
                new LevelTestQuestionDto(6, "「この ケーキは（　）ですね。」에 들어갈 가장 자연스러운 말은?", "중간",
                        List.of(
                                new OptionDto(1, "おいしい"),
                                new OptionDto(2, "おいしく"),
                                new OptionDto(3, "おいしさ"),
                                new OptionDto(4, "おいしがる"),
                                new OptionDto(5, "모르겠어요")
                        ), 1),
                new LevelTestQuestionDto(7, "「しゅくだいが おわった（　）、ゲームをします。」", "중간",
                        List.of(
                                new OptionDto(1, "から"),
                                new OptionDto(2, "まで"),
                                new OptionDto(3, "でも"),
                                new OptionDto(4, "しか"),
                                new OptionDto(5, "모르겠어요")
                        ), 1),
                new LevelTestQuestionDto(8, "「ごはんを たべたあとで、コーヒーを（　）。」", "중간",
                        List.of(
                                new OptionDto(1, "のみます"),
                                new OptionDto(2, "かきます"),
                                new OptionDto(3, "つかいます"),
                                new OptionDto(4, "たちます"),
                                new OptionDto(5, "모르겠어요")
                        ), 1),
                new LevelTestQuestionDto(9, "「きょうは きのうより（　）です。」에 들어갈 알맞은 말은?", "중간",
                        List.of(
                                new OptionDto(1, "さむい"),
                                new OptionDto(2, "さむく"),
                                new OptionDto(3, "さむさ"),
                                new OptionDto(4, "さむがり"),
                                new OptionDto(5, "모르겠어요")
                        ), 1),
                new LevelTestQuestionDto(10, "「わたしは 日本語を（　）べんきょうしています。」", "중간",
                        List.of(
                                new OptionDto(1, "まだ"),
                                new OptionDto(2, "とても"),
                                new OptionDto(3, "いつも"),
                                new OptionDto(4, "すこし"),
                                new OptionDto(5, "모르겠어요")
                        ), 4)
        );
    }

    // Case 3 문제 데이터
    private List<LevelTestQuestionDto> getJapaneseCase3Questions() {
        return List.of(
                new LevelTestQuestionDto(1, "「雨が ふっていた（　）、出かけませんでした。」", "쉬움",
                        List.of(
                                new OptionDto(1, "ので"),
                                new OptionDto(2, "まで"),
                                new OptionDto(3, "しか"),
                                new OptionDto(4, "ほど"),
                                new OptionDto(5, "모르겠어요")
                        ), 1),
                new LevelTestQuestionDto(2, "「この 本は むずかしいですが、とても（　）です。」", "쉬움",
                        List.of(
                                new OptionDto(1, "べんり"),
                                new OptionDto(2, "ゆうめい"),
                                new OptionDto(3, "おもしろい"),
                                new OptionDto(4, "しずか"),
                                new OptionDto(5, "모르겠어요")
                        ), 3),
                new LevelTestQuestionDto(3, "「まだ 宿題が 終わっていないので、今日は 遊びに 行け（　）。」", "쉬움",
                        List.of(
                                new OptionDto(1, "ます"),
                                new OptionDto(2, "ません"),
                                new OptionDto(3, "たい"),
                                new OptionDto(4, "そう"),
                                new OptionDto(5, "모르겠어요")
                        ), 2),
                new LevelTestQuestionDto(4, "다음 중 가장 자연스러운 문장은?", "쉬움",
                        List.of(
                                new OptionDto(1, "毎日 日本語を 勉強したいです。"),
                                new OptionDto(2, "毎日 日本語が 勉強を します。"),
                                new OptionDto(3, "毎日 日本語で 勉強が した。"),
                                new OptionDto(4, "毎日 日本語に 勉強します。"),
                                new OptionDto(5, "모르겠어요")
                        ), 1),
                new LevelTestQuestionDto(5, "「わからないことが あれば、あとで 質問しても（　）です。」", "쉬움",
                        List.of(
                                new OptionDto(1, "じょうぶ"),
                                new OptionDto(2, "いい"),
                                new OptionDto(3, "うまい"),
                                new OptionDto(4, "たいへん"),
                                new OptionDto(5, "모르겠어요")
                        ), 2),
                new LevelTestQuestionDto(6, "「日本語を もっと じょうずに 話せる（　）なりたいです。」", "어려움",
                        List.of(
                                new OptionDto(1, "しか"),
                                new OptionDto(2, "ように"),
                                new OptionDto(3, "ほど"),
                                new OptionDto(4, "だけ"),
                                new OptionDto(5, "모르겠어요")
                        ), 2),
                new LevelTestQuestionDto(7, "「電車が おくれた（　）、会議に まにあいませんでした。」", "어려움",
                        List.of(
                                new OptionDto(1, "ため"),
                                new OptionDto(2, "だけ"),
                                new OptionDto(3, "など"),
                                new OptionDto(4, "こそ"),
                                new OptionDto(5, "모르겠어요")
                        ), 1),
                new LevelTestQuestionDto(8, "「この かばんは 軽くて、たくさん 入る（　）便利です。」", "어려움",
                        List.of(
                                new OptionDto(1, "ので"),
                                new OptionDto(2, "のに"),
                                new OptionDto(3, "でも"),
                                new OptionDto(4, "しか"),
                                new OptionDto(5, "모르겠어요")
                        ), 1),
                new LevelTestQuestionDto(9, "「先生が いらっしゃる前に、教室を（　）おいてください。」", "어려움",
                        List.of(
                                new OptionDto(1, "かたづけて"),
                                new OptionDto(2, "かたづける"),
                                new OptionDto(3, "かたづけない"),
                                new OptionDto(4, "かたづけろ"),
                                new OptionDto(5, "모르겠어요")
                        ), 1),
                new LevelTestQuestionDto(10, "다음 문장의 뜻으로 가장 알맞은 것은?\n「時間が あれば、日本の 小説を 読んでみたいです。」", "어려움",
                        List.of(
                                new OptionDto(1, "시간이 없어서 일본 소설을 읽을 수 없다"),
                                new OptionDto(2, "시간이 있으면 일본 소설을 한번 읽어보고 싶다"),
                                new OptionDto(3, "일본 소설은 이미 다 읽었다"),
                                new OptionDto(4, "일본 소설보다 만화를 더 좋아한다"),
                                new OptionDto(5, "모르겠어요")
                        ), 2)
        );
    }

    // 영어 Case 1: A1 ~ A2 초반 수준
    private List<LevelTestQuestionDto> getEnglishCase1Questions() {
        return List.of(
                new LevelTestQuestionDto(1, "빈칸에 들어갈 가장 알맞은 것은?\nMy sister ___ a teacher.", "쉬움",
                        List.of(
                                new OptionDto(1, "am"),
                                new OptionDto(2, "is"),
                                new OptionDto(3, "are"),
                                new OptionDto(4, "be"),
                                new OptionDto(5, "모르겠어요")
                        ), 2),
                new LevelTestQuestionDto(2, "'borrow'의 뜻으로 가장 알맞은 것은?", "쉬움",
                        List.of(
                                new OptionDto(1, "빌려주다"),
                                new OptionDto(2, "구매하다"),
                                new OptionDto(3, "빌리다"),
                                new OptionDto(4, "돌려주다"),
                                new OptionDto(5, "모르겠어요")
                        ), 3),
                new LevelTestQuestionDto(3, "빈칸에 들어갈 가장 알맞은 것은?\nTom ___ to school by bus every day.", "쉬움",
                        List.of(
                                new OptionDto(1, "go"),
                                new OptionDto(2, "goes"),
                                new OptionDto(3, "going"),
                                new OptionDto(4, "went"),
                                new OptionDto(5, "모르겠어요")
                        ), 2),
                new LevelTestQuestionDto(4, "질문에 가장 자연스러운 대답은?\nHow often do you exercise?", "쉬움",
                        List.of(
                                new OptionDto(1, "At the gym."),
                                new OptionDto(2, "For two hours."),
                                new OptionDto(3, "Twice a week."),
                                new OptionDto(4, "With my friend."),
                                new OptionDto(5, "모르겠어요")
                        ), 3),
                new LevelTestQuestionDto(5, "빈칸에 들어갈 가장 알맞은 것은?\nThe class starts ___ 9 a.m.", "쉬움",
                        List.of(
                                new OptionDto(1, "in"),
                                new OptionDto(2, "on"),
                                new OptionDto(3, "at"),
                                new OptionDto(4, "from"),
                                new OptionDto(5, "모르겠어요")
                        ), 3),
                new LevelTestQuestionDto(6, "빈칸에 들어갈 가장 알맞은 것은?\nShe can ___ the piano very well.", "쉬움",
                        List.of(
                                new OptionDto(1, "plays"),
                                new OptionDto(2, "played"),
                                new OptionDto(3, "playing"),
                                new OptionDto(4, "play"),
                                new OptionDto(5, "모르겠어요")
                        ), 4),
                new LevelTestQuestionDto(7, "빈칸에 들어갈 가장 알맞은 것은?\nWe ___ to the museum yesterday.", "쉬움",
                        List.of(
                                new OptionDto(1, "go"),
                                new OptionDto(2, "goes"),
                                new OptionDto(3, "went"),
                                new OptionDto(4, "going"),
                                new OptionDto(5, "모르겠어요")
                        ), 3),
                new LevelTestQuestionDto(8, "친구가 시험에 합격했다고 말했을 때 가장 자연스러운 표현은?", "쉬움",
                        List.of(
                                new OptionDto(1, "I'm sorry to hear that."),
                                new OptionDto(2, "Congratulations!"),
                                new OptionDto(3, "Never mind."),
                                new OptionDto(4, "You're welcome."),
                                new OptionDto(5, "모르겠어요")
                        ), 2),
                new LevelTestQuestionDto(9, "'No food or drinks.'의 의미로 가장 알맞은 것은?", "쉬움",
                        List.of(
                                new OptionDto(1, "음식과 음료를 무료로 제공합니다."),
                                new OptionDto(2, "음식만 가지고 들어갈 수 있습니다."),
                                new OptionDto(3, "음료만 가지고 들어갈 수 있습니다."),
                                new OptionDto(4, "음식과 음료를 가지고 들어갈 수 없습니다."),
                                new OptionDto(5, "모르겠어요")
                        ), 4),
                new LevelTestQuestionDto(10, "글을 읽고 Mina가 버스를 탄 이유를 고르세요.\nIt was raining this morning, so Mina took the bus instead of walking to school.", "쉬움",
                        List.of(
                                new OptionDto(1, "늦잠을 자서"),
                                new OptionDto(2, "비가 와서"),
                                new OptionDto(3, "학교가 멀어서"),
                                new OptionDto(4, "친구를 만나서"),
                                new OptionDto(5, "모르겠어요")
                        ), 2)
        );
    }

    // 영어 Case 2: A2 ~ B1 수준
    private List<LevelTestQuestionDto> getEnglishCase2Questions() {
        return List.of(
                new LevelTestQuestionDto(1, "빈칸에 들어갈 가장 알맞은 것은?\nI ___ dinner when you called me.", "쉬움",
                        List.of(
                                new OptionDto(1, "cook"),
                                new OptionDto(2, "cooked"),
                                new OptionDto(3, "was cooking"),
                                new OptionDto(4, "am cooking"),
                                new OptionDto(5, "모르겠어요")
                        ), 3),
                new LevelTestQuestionDto(2, "빈칸에 들어갈 가장 알맞은 것은?\nThis book is ___ than the one I read last week.", "쉬움",
                        List.of(
                                new OptionDto(1, "interesting"),
                                new OptionDto(2, "more interesting"),
                                new OptionDto(3, "most interesting"),
                                new OptionDto(4, "the interesting"),
                                new OptionDto(5, "모르겠어요")
                        ), 2),
                new LevelTestQuestionDto(3, "빈칸에 들어갈 가장 알맞은 것은?\nWe don't have ___ milk left.", "쉬움",
                        List.of(
                                new OptionDto(1, "some"),
                                new OptionDto(2, "any"),
                                new OptionDto(3, "many"),
                                new OptionDto(4, "a few"),
                                new OptionDto(5, "모르겠어요")
                        ), 2),
                new LevelTestQuestionDto(4, "빈칸에 들어갈 가장 알맞은 것은?\nYou ___ use your phone during the exam.", "쉬움",
                        List.of(
                                new OptionDto(1, "don't have to"),
                                new OptionDto(2, "should"),
                                new OptionDto(3, "mustn't"),
                                new OptionDto(4, "might"),
                                new OptionDto(5, "모르겠어요")
                        ), 3),
                new LevelTestQuestionDto(5, "대화의 빈칸에 들어갈 가장 자연스러운 것은?\nA: Would you like some coffee?\nB: ___", "쉬움",
                        List.of(
                                new OptionDto(1, "Yes, please."),
                                new OptionDto(2, "It doesn't matter."),
                                new OptionDto(3, "That's all right."),
                                new OptionDto(4, "I hope so."),
                                new OptionDto(5, "모르겠어요")
                        ), 1),
                new LevelTestQuestionDto(6, "빈칸에 들어갈 가장 알맞은 것은?\nI ___ in Seoul since 2022.", "중간",
                        List.of(
                                new OptionDto(1, "live"),
                                new OptionDto(2, "lived"),
                                new OptionDto(3, "have lived"),
                                new OptionDto(4, "am living yesterday"),
                                new OptionDto(5, "모르겠어요")
                        ), 3),
                new LevelTestQuestionDto(7, "빈칸에 들어갈 가장 알맞은 것은?\nIf it rains tomorrow, we ___ at home.", "중간",
                        List.of(
                                new OptionDto(1, "stayed"),
                                new OptionDto(2, "will stay"),
                                new OptionDto(3, "have stayed"),
                                new OptionDto(4, "would have stayed"),
                                new OptionDto(5, "모르겠어요")
                        ), 2),
                new LevelTestQuestionDto(8, "빈칸에 들어갈 가장 알맞은 것은?\nThe woman ___ helped me was very kind.", "중간",
                        List.of(
                                new OptionDto(1, "which"),
                                new OptionDto(2, "where"),
                                new OptionDto(3, "whose"),
                                new OptionDto(4, "who"),
                                new OptionDto(5, "모르겠어요")
                        ), 4),
                new LevelTestQuestionDto(9, "문장에서 'afford'의 의미로 가장 알맞은 것은?\nI want to buy the laptop, but I can't afford it right now.", "중간",
                        List.of(
                                new OptionDto(1, "노트북을 찾을 수 없다"),
                                new OptionDto(2, "노트북을 사용할 수 없다"),
                                new OptionDto(3, "노트북을 살 경제적 여유가 없다"),
                                new OptionDto(4, "노트북을 선택할 수 없다"),
                                new OptionDto(5, "모르겠어요")
                        ), 3),
                new LevelTestQuestionDto(10, "글을 읽고 내용과 일치하는 것을 고르세요.\nDaniel usually drives to work. However, his car is being repaired this week, so he is taking the subway. He says the subway is more convenient than he expected.", "중간",
                        List.of(
                                new OptionDto(1, "Daniel은 항상 지하철로 출근한다."),
                                new OptionDto(2, "Daniel은 자동차를 새로 구입했다."),
                                new OptionDto(3, "Daniel은 이번 주에 출근하지 않는다."),
                                new OptionDto(4, "Daniel은 지하철이 예상보다 편리하다고 생각한다."),
                                new OptionDto(5, "모르겠어요")
                        ), 4)
        );
    }

    // 영어 Case 3: B1 ~ B2 수준
    private List<LevelTestQuestionDto> getEnglishCase3Questions() {
        return List.of(
                new LevelTestQuestionDto(1, "빈칸에 들어갈 가장 알맞은 것은?\nThis bridge ___ in 1995.", "쉬움",
                        List.of(
                                new OptionDto(1, "built"),
                                new OptionDto(2, "was built"),
                                new OptionDto(3, "has built"),
                                new OptionDto(4, "was building"),
                                new OptionDto(5, "모르겠어요")
                        ), 2),
                new LevelTestQuestionDto(2, "빈칸에 들어갈 가장 알맞은 것은?\nI ___ play outside every day when I was a child.", "쉬움",
                        List.of(
                                new OptionDto(1, "used to"),
                                new OptionDto(2, "am used to"),
                                new OptionDto(3, "was used"),
                                new OptionDto(4, "use to"),
                                new OptionDto(5, "모르겠어요")
                        ), 1),
                new LevelTestQuestionDto(3, "빈칸에 들어갈 가장 알맞은 것은?\nI met a writer ___ books have been translated into many languages.", "쉬움",
                        List.of(
                                new OptionDto(1, "who"),
                                new OptionDto(2, "which"),
                                new OptionDto(3, "whose"),
                                new OptionDto(4, "whom"),
                                new OptionDto(5, "모르겠어요")
                        ), 3),
                new LevelTestQuestionDto(4, "빈칸에 들어갈 가장 알맞은 것은?\nYou won't finish the project on time ___ you start working now.", "쉬움",
                        List.of(
                                new OptionDto(1, "if"),
                                new OptionDto(2, "unless"),
                                new OptionDto(3, "because"),
                                new OptionDto(4, "although"),
                                new OptionDto(5, "모르겠어요")
                        ), 2),
                new LevelTestQuestionDto(5, "Jane said, 'I am tired.'를 간접화법으로 바르게 바꾼 것은?", "쉬움",
                        List.of(
                                new OptionDto(1, "Jane said that I am tired."),
                                new OptionDto(2, "Jane said that she is tired yesterday."),
                                new OptionDto(3, "Jane said that she was tired."),
                                new OptionDto(4, "Jane said that she has tired."),
                                new OptionDto(5, "모르겠어요")
                        ), 3),
                new LevelTestQuestionDto(6, "빈칸에 들어갈 가장 알맞은 것은?\nIf I had known about the meeting, I ___ it.", "어려움",
                        List.of(
                                new OptionDto(1, "attended"),
                                new OptionDto(2, "would attend"),
                                new OptionDto(3, "would have attended"),
                                new OptionDto(4, "had attended"),
                                new OptionDto(5, "모르겠어요")
                        ), 3),
                new LevelTestQuestionDto(7, "빈칸에 들어갈 가장 알맞은 것은?\nNot until she arrived home ___ that she had lost her wallet.", "어려움",
                        List.of(
                                new OptionDto(1, "she realized"),
                                new OptionDto(2, "did she realize"),
                                new OptionDto(3, "she had realized"),
                                new OptionDto(4, "had she realized"),
                                new OptionDto(5, "모르겠어요")
                        ), 2),
                new LevelTestQuestionDto(8, "다음 중 문법적으로 가장 자연스러운 문장은?", "어려움",
                        List.of(
                                new OptionDto(1, "Despite the weather was bad, we went hiking."),
                                new OptionDto(2, "Despite of the bad weather, we went hiking."),
                                new OptionDto(3, "Despite the bad weather, we went hiking."),
                                new OptionDto(4, "Despite it was bad weather, we went hiking."),
                                new OptionDto(5, "모르겠어요")
                        ), 3),
                new LevelTestQuestionDto(9, "문장에서 'reluctant'의 의미로 가장 알맞은 것은?\nShe was reluctant to accept the offer because it required moving abroad.", "어려움",
                        List.of(
                                new OptionDto(1, "기꺼이 하는"),
                                new OptionDto(2, "망설이는"),
                                new OptionDto(3, "자격이 있는"),
                                new OptionDto(4, "준비가 끝난"),
                                new OptionDto(5, "모르겠어요")
                        ), 2),
                new LevelTestQuestionDto(10, "글을 읽고 가장 타당하게 추론할 수 있는 것을 고르세요.\nThe company introduced remote work expecting productivity to rise immediately. Output remained nearly unchanged during the first month, but employee satisfaction increased significantly. Managers believe productivity may improve once teams become accustomed to the new communication tools.", "어려움",
                        List.of(
                                new OptionDto(1, "원격 근무는 첫 달부터 생산성을 크게 높였다."),
                                new OptionDto(2, "직원 만족도와 생산성이 모두 감소했다."),
                                new OptionDto(3, "회사는 원격 근무를 즉시 폐지할 예정이다."),
                                new OptionDto(4, "새로운 도구에 적응한 뒤 생산성이 향상될 가능성이 있다."),
                                new OptionDto(5, "모르겠어요")
                        ), 4)
        );
    }

    /**
     * 레벨 테스트 결과 채점 및 레벨 산정
     *
     * * 사용자의 사전 질문 응답을 기반으로 문제 세트를 결정한 뒤,
     * 제출한 답안을 채점하여 점수, 정답 수, 오답 수를 계산한다.
     *
     * * 이후 문제 세트별 점수 기준에 따라 최종 레벨을 산정한다.
     *
     * @param dto 레벨 테스트 제출 정보
     * @param loginId 로그인 ID
     * @return 채점 결과 및 레벨 정보
     * @throws BusinessException 사용자가 존재하지 않는 경우
     */
    public LevelTestResultResponseDto levelTestResult(LevelTestResultRequestDto dto, String loginId) {

        User user = findUser(loginId);

        // 케이스 분류
        int caseNumber = determineCase(dto.getPre1Answer(), dto.getPre2Answer(), dto.getPre3Answer());

        // 문제 가져오기
        List<LevelTestQuestionDto> questions = getQuestionsByCase(caseNumber, user.getLearningLanguage());

        int score = 0;
        int correctCount = 0;
        int wrongCount = 0;

        for (int i = 0; i < questions.size(); i++) {
            LevelTestQuestionDto question = questions.get(i);
            int userAnswer = dto.getAnswers().get(i);

            if (userAnswer == question.getCorrectAnswer()) {
                correctCount++;
                // 문제 난이도별 가중치 적용
                if (caseNumber == 1) {
                    score += 10;
                } else {
                    score += switch (question.getDifficulty()) {
                        case "쉬움" -> 8;
                        case "중간", "어려움" -> 12;
                        default -> 10;
                    };
                }
            } else {
                wrongCount++;
            }
        }

        // 레벨 산정
        int level = calculateLevel(caseNumber, score);

        return new LevelTestResultResponseDto(score, correctCount, wrongCount, level);
    }

    /**
     * 레벨 계산
     *
     * * 동일한 점수라도 문제 세트 난이도가 다르므로
     * 세트별로 서로 다른 레벨 기준을 적용한다.
     *
     * @param caseNumber 문제 세트 번호
     * @param score 획득 점수
     * @return 최종 레벨
     */
    private int calculateLevel(int caseNumber, int score) {
        return switch (caseNumber) {
            case 1 -> {
                if (score < 25) yield 1;
                if (score < 50) yield 2;
                if (score < 75) yield 3;
                yield 4;
            }
            case 2 -> {
                if (score < 20) yield 3;
                if (score < 40) yield 4;
                if (score < 60) yield 5;
                if (score < 80) yield 6;
                yield 7;
            }
            default -> {
                if (score < 17) yield 5;
                if (score < 34) yield 6;
                if (score < 51) yield 7;
                if (score < 68) yield 8;
                if (score < 85) yield 9;
                yield 10;
            }
        };
    }

    /**
     * 레벨 테스트 결과 저장
     *
     * * 인증된 사용자의 레벨을 갱신하여
     * 이후 학습 콘텐츠 추천 및 난이도 설정에 활용한다.
     *
     * @param dto 저장할 레벨 정보
     * @throws BusinessException 인증된 사용자가 존재하지 않는 경우
     */
    @Transactional
    public void saveLevel(SaveLevelRequestDto dto) {

        String loginId = SecurityContextHolder.getContext().getAuthentication().getName();

        User user = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        user.updateLevel(dto.getLevel());
    }

    private User findUser(String loginId) {
        return userRepository.findByLoginIdAndIsDeletedFalse(loginId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }
}
