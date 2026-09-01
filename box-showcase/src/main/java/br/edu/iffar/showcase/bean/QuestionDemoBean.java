package br.edu.iffar.showcase.bean;

import br.edu.iffar.box.component.question.QuestionModel;
import br.edu.iffar.box.component.question.QuestionOption;
import br.edu.iffar.box.component.question.QuestionType;
import br.edu.iffar.box.component.question.SimpleQuestionModel;
import br.edu.iffar.box.component.question.SimpleQuestionOption;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;

import java.io.Serializable;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Backing bean for b:question demo page in box-showcase.
 */
@Named
@RequestScoped
public class QuestionDemoBean implements Serializable {

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");

    // Declarative examples
    private String feedbackText;
    private String selectedPriority = "MEDIUM";
    private List<QuestionOption> priorityOptions;

    // Model-driven assessment questions (simulating database records)
    private List<SimpleQuestionModel> assessmentQuestions;
    private Map<Object, String> answers = new HashMap<>();

    private String lastAjaxLog;
    private String submissionResult;

    @PostConstruct
    public void init() {
        // Priority options for declarative demo
        priorityOptions = List.of(
                new SimpleQuestionOption("LOW", "Low Priority", "Can be addressed whenever convenient."),
                new SimpleQuestionOption("MEDIUM", "Medium Priority", "Standard operational workflow."),
                new SimpleQuestionOption("HIGH", "High Priority", "Urgent requirement requiring prompt attention."),
                new SimpleQuestionOption("CRITICAL", "Critical", "Blocking issue requiring immediate action.")
        );

        // Simulated questions from a database
        assessmentQuestions = new ArrayList<>();

        SimpleQuestionModel q1 = new SimpleQuestionModel(
                101L,
                "How satisfied are you with the process automation results?",
                QuestionType.CHOICE,
                true
        );
        q1.setDescription("Choose the alternative that best reflects your experience.");
        q1.addOption("VERY_SATISFIED", "Very Satisfied", "The process is seamless and saves significant time.")
          .addOption("SATISFIED", "Satisfied", "The process meets expectations.")
          .addOption("NEUTRAL", "Neutral", "Average results with room for optimization.")
          .addOption("DISSATISFIED", "Dissatisfied", "Frequent delays or difficulties encountered.");
        assessmentQuestions.add(q1);

        SimpleQuestionModel q2 = new SimpleQuestionModel(
                102L,
                "What specific improvements or features would you recommend for this workflow?",
                QuestionType.DESCRIPTIVE,
                false
        );
        q2.setDescription("Optional detailed feedback or suggestions.");
        assessmentQuestions.add(q2);

        SimpleQuestionModel q3 = new SimpleQuestionModel(
                103L,
                "Would you recommend this process to other departments?",
                QuestionType.CHOICE,
                true
        );
        q3.addOption("YES", "Yes, definitely")
          .addOption("MAYBE", "Maybe, with modifications")
          .addOption("NO", "No");
        assessmentQuestions.add(q3);
    }

    public void onAssessmentChange(jakarta.faces.event.AjaxBehaviorEvent event) {
        onAssessmentChange();
    }

    public void onAssessmentChange() {
        this.lastAjaxLog = String.format("Auto-saved assessment answers (%d recorded) at %s",
                answers.size(), LocalTime.now().format(TIME_FORMAT));
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Answers Recorded", this.lastAjaxLog));
    }

    public void onAjaxAnswerChange(Object questionId) {
        String answer = answers.get(questionId);
        this.lastAjaxLog = String.format("Auto-saved question #%s at %s (Answer: %s)",
                questionId, LocalTime.now().format(TIME_FORMAT), answer);
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Answer Recorded", this.lastAjaxLog));
    }

    public void submitAssessment() {
        StringBuilder sb = new StringBuilder();
        sb.append("Assessment submitted successfully at ").append(LocalTime.now().format(TIME_FORMAT)).append(":\n");
        for (SimpleQuestionModel q : assessmentQuestions) {
            String ans = answers.get(q.getId());
            sb.append("• ").append(q.getPrompt()).append(" -> ")
              .append(ans != null && !ans.isBlank() ? ans : "(no response)")
              .append("\n");
        }
        this.submissionResult = sb.toString();
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Assessment Submitted", "All answers were recorded."));
    }

    // Getters and Setters

    public String getFeedbackText() {
        return feedbackText;
    }

    public void setFeedbackText(String feedbackText) {
        this.feedbackText = feedbackText;
    }

    public String getSelectedPriority() {
        return selectedPriority;
    }

    public void setSelectedPriority(String selectedPriority) {
        this.selectedPriority = selectedPriority;
    }

    public List<QuestionOption> getPriorityOptions() {
        return priorityOptions;
    }

    public List<SimpleQuestionModel> getAssessmentQuestions() {
        return assessmentQuestions;
    }

    public Map<Object, String> getAnswers() {
        return answers;
    }

    public void setAnswers(Map<Object, String> answers) {
        this.answers = answers;
    }

    public String getLastAjaxLog() {
        return lastAjaxLog;
    }

    public String getSubmissionResult() {
        return submissionResult;
    }
}
