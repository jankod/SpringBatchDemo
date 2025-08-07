package hr.biosoft.batch.reader;

public class ProgressBarExample {

    public static void main(String[] args) throws InterruptedException {
        int total = 100;

        for (int i = 0; i <= total; i++) {
            // Generiranje progress bara
            String progressBar = createProgressBar(i, total);

            // Ispis u konzolu
            // "\r" vraća kursor na početak retka
            System.out.print("\r" + progressBar);

            // Pauza od 50 milisekundi da se vidi napredak
            Thread.sleep(50);
        }

        // Nakon završetka, ispis novog retka da se kursor pomakne dolje
        System.out.println("\nZavršeno!");
    }

    private static String createProgressBar(int current, int total) {
        int barLength = 50; // Duljina progress bara
        int progress = (int) (((double) current / total) * barLength);

        StringBuilder sb = new StringBuilder();

        // Crtanje popunjenog dijela
        for (int i = 0; i < progress; i++) {
            sb.append("=");
        }

        // Crtanje praznog dijela
        for (int i = progress; i < barLength; i++) {
            sb.append(" ");
        }

        // Dodavanje postotka
        int percentage = (int) (((double) current / total) * 100);
        return String.format("[%s] %d%%", sb.toString(), percentage);
    }
}
