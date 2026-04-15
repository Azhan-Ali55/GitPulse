import com.gitpulse.model.Commit;
import com.gitpulse.model.Contributor;
import com.gitpulse.model.Repository;
import com.gitpulse.service.AiSummaryService;
import com.gitpulse.service.DataService;
import com.gitpulse.service.PromptGenerator;
import com.gitpulse.service.RepositorySummaryGenerator;

public class GitPulse {
    public static void main(String[] args) {
        DataService service = new DataService();
        Repository repo = service.loadRepository("Azhan-Ali55", "sudoku");
        PromptGenerator generator = new RepositorySummaryGenerator(repo);
        AiSummaryService aiService = new AiSummaryService();
        String summary = aiService.getSummary(generator);
        System.out.println(summary);
    }
}