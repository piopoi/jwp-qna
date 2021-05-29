package qna.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import qna.config.TestDataSourceConfig;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static qna.domain.UserTest.JAVAJIGI;
import static qna.domain.UserTest.SANJIGI;

@TestDataSourceConfig
public class QuestionTest {
    public static final Question Q1 = new Question("title1", "contents1").writeBy(JAVAJIGI);
    public static final Question Q2 = new Question("title2", "contents2").writeBy(SANJIGI);

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AnswerRepository answerRepository;

    @BeforeEach
    void setUp() {
        Q1.writeBy(userRepository.save(JAVAJIGI));
        Q2.writeBy(userRepository.save(SANJIGI));
    }

    @DisplayName("question 저장 검증")
    @Test
    void saveTest() {
        Question saved = questionRepository.save(Q1);

        assertNotNull(saved.getId());
        assertEquals(Q1.getTitle(), saved.getTitle());
        assertEquals(Q1.getContents(), saved.getContents());
    }

    @DisplayName("삭제되지 않은 데이터 찾아오기")
    @Test
    void findByIdAndDeletedFalseTest() {

        Question expected = questionRepository.save(Q1);
        Question actual = questionRepository.findByIdAndDeletedFalse(expected.getId())
                                            .orElseThrow(IllegalArgumentException::new);

        assertFalse(actual.isDeleted());
        equals(expected, actual);
    }

    @DisplayName("findByDeletedFalse 검증")
    @Test
    void findByDeletedFalseTest() {

        List<Question> expected = new ArrayList<>();
        expected.add(questionRepository.save(Q1));
        expected.add(questionRepository.save(Q2));

        List<Question> actual = questionRepository.findByDeletedFalse();

        for (int i = 0; i < expected.size(); i++) {
            assertFalse(actual.get(i).isDeleted());
            equals(expected.get(i), actual.get(i));
        }
    }

    @DisplayName("getAnswers는 지워지지 않은 답변만 가져옴")
    @Test
    void getAnswersTest() {

        User user = new User("id", "pwd", "name", "email");
        user = userRepository.save(user);

        Question question = new Question("title", "contents");
        question = questionRepository.save(question);

        Answer answer1 = new Answer(user, question, "contents1");
        answer1 = answerRepository.save(answer1);
        question.addAnswer(answer1);

        Answer answer2 = new Answer(user, question, "contents2");
        answer2 = answerRepository.save(answer2);
        question.addAnswer(answer2);

        question.deleteAnswer(answer2);
        assertTrue(answer2.isDeleted());

        questionRepository.flush();

        Optional<Question> actual = questionRepository.findById(question.getId());
        assertTrue(actual.isPresent());

        List<Answer> answers = actual.get().getAnswers();
        assertEquals(1, answers.size());

        for (Answer answer : answers) {
            assertFalse(answer.isDeleted());
        }
    }

    private void equals(Question expected, Question actual) {
        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getTitle(), actual.getTitle());
        assertEquals(expected.getContents(), actual.getContents());
        assertEquals(expected.getWriter(), actual.getWriter());
    }
}
