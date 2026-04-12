import com.gitpulse.model.Commit;
import com.gitpulse.model.Contributor;
import com.gitpulse.model.Repository;
import com.gitpulse.service.DataService;

public class GitPulse {
    public static void main(String[] args) {

        DataService service = new DataService();

        // Using a small public repo for testing
        Repository repo = service.loadRepository("Azhan-Ali55", "sudoku");

        // Test 1 — basic repo info
        System.out.println("=== REPOSITORY ===");
        System.out.println("Name: "        + repo.getName());
        System.out.println("Owner: "       + repo.getOwner());
        System.out.println("Description: " + repo.getDescription());
        System.out.println("Language: "    + repo.getLanguage());

        // Test 2 — commits
        System.out.println("\n=== COMMITS ===");
        System.out.println("Total commits fetched: " + repo.getCommits().size());
        if (!repo.getCommits().isEmpty()) {
            Commit first = repo.getCommits().get(0);
            System.out.println("Latest commit: " + first.getMessage());
            System.out.println("By: "            + first.getAuthorName());
            System.out.println("On: "            + first.getDate());
        }

        // Test 3 — contributors
        System.out.println("\n=== CONTRIBUTORS ===");
        System.out.println("Total contributors: " + repo.getContributors().size());
        if (!repo.getContributors().isEmpty()) {
            Contributor top = repo.getContributors().get(0);
            System.out.println("Top contributor: " + top.getUsername());
            System.out.println("Total commits: "   + top.getTotalCommits());
        }
    }
}