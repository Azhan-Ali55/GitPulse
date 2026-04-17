import com.gitpulse.model.Contributor;
import com.gitpulse.model.Repository;
import com.gitpulse.service.DataService;

public class GitPulse {
    public static void main(String[] args) {

        DataService service = new DataService();
        Repository repo = service.loadRepository("Azhan-Ali55", "sudoku");

        // Test 1 — basic repo info
        System.out.println("=== REPOSITORY ===");
        System.out.println("Name: "        + repo.getName());
        System.out.println("Owner: "       + repo.getOwner());
        System.out.println("Description: " + repo.getDescription());
        System.out.println("Language: "    + repo.getLanguage());

        // Test 2 — commits
        System.out.println("\n=== COMMITS ===");
        System.out.println("Total commits: " + repo.getCommits().size());
        if (!repo.getCommits().isEmpty()) {
            System.out.println("Latest: " + repo.getCommits().get(0).getMessage());
            System.out.println("By: "     + repo.getCommits().get(0).getAuthorName());
            System.out.println("On: "     + repo.getCommits().get(0).getDate());
        }

        // Test 3 — contributors
        System.out.println("\n=== CONTRIBUTORS ===");
        System.out.println("Total: " + repo.getContributors().size());
        for (Contributor c : repo.getContributors()) {
            System.out.println("- " + c.getUsername() + ": " + c.getTotalCommits() + " commits");
        }

        // Test 4 — readme
        System.out.println("\n=== README ===");
        System.out.println(repo.getReadme() != null ? "README loaded successfully" : "No README");

        // Test 5 — weekly stats
        System.out.println("\n=== WEEKLY STATS ===");
        var weekly = repo.getWeeklyActivity();

        if (weekly == null || weekly.isEmpty()) {
            System.out.println("No stats available");
        } else {
            for (var entry : weekly.entrySet()) {
                System.out.println("\nWeek starting: " + entry.getKey());
                for (var commit : entry.getValue()) {
                    System.out.println("- " + commit.getMessage() +
                            " by " + commit.getAuthorName());
                }
            }
        }

        // Test 6 — AI repository summary
        System.out.println("\n=== AI REPOSITORY SUMMARY ===");
        System.out.println(service.getRepositorySummary(repo));

        // Test 7 — AI readme summary
        System.out.println("\n=== AI README SUMMARY ===");
        System.out.println(service.getReadmeSummary(repo));
    }
}