package com.union.brainrush.service;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import com.union.brainrush.repository.QuestionRepository;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.union.brainrush.model.QuestionFormat;
import org.springframework.transaction.annotation.Transactional;

@Component
public class QuestionService {
    private final ArrayList<QuestionFormat> qFormats = new ArrayList<>();
    private final QuestionRepository repository;
//    public QuestionService() {
//        loadQuestions();
//    }
    public QuestionService(QuestionRepository repository) {
        this.repository = repository;
        if (repository.count() == 0) {
            seedDatabase();
        }
    }
    private void loadQuestions() {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("data/data.json")) {
            ObjectMapper mapper = new ObjectMapper();
            ArrayList<QuestionFormat> loadedUsers = mapper.readValue(inputStream, new TypeReference<ArrayList<QuestionFormat>>() {});
            qFormats.addAll(loadedUsers);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void seedDatabase() {
        try (InputStream is = getClass().getResourceAsStream("/data/data.json")) {
            ObjectMapper mapper = new ObjectMapper();
            List<QuestionFormat> list = mapper.readValue(is, new TypeReference<List<QuestionFormat>>() {});

            List<QuestionFormat> cleanList = new ArrayList<>();

            for (QuestionFormat q : list) {
                q.setId(0);

                if (q.getAns() != null) {
                    q.getAns().entrySet().removeIf(entry ->
                            entry.getKey() == null || entry.getKey().trim().isEmpty()
                    );

                    if (!q.getAns().isEmpty()) {
                        cleanList.add(q);
                    }
                }
            }

            repository.saveAll(cleanList);
            System.out.println("Successfully seeded " + cleanList.size() + " questions.");
        } catch (Exception e) {
            System.err.println("Critical Error during seeding: " + e.getMessage());
            e.printStackTrace();
        }
    }
    public List<QuestionFormat> getQuestions() {
        return repository.findAll();
    }
    public void addQuestion(QuestionFormat qFormat) {
        repository.save(qFormat);
    }

    @Transactional
    public void deleteQuestion(long id) {
        repository.deleteById(id);
    }

    @Transactional
    public void saveOrUpdate(QuestionFormat question) {
        repository.save(question);
    }

    @Transactional
    public void batchSave(List<QuestionFormat> questions) {
        repository.saveAll(questions);
    }

    @Transactional
    public void deleteAll(List<QuestionFormat> questions) {
        repository.deleteAll(questions);
    }
    public List<QuestionFormat> getRandomRound(int totalQuestions) {
        return repository.findRandomQuestions(totalQuestions);
    }
}
