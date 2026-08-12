package br.com.mineradortimemania.app;

import android.app.*;
import android.os.*;
import android.content.*;
import android.graphics.*;
import android.graphics.drawable.*;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.*;
import android.widget.*;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;

public class MainActivity extends Activity {

    static final int GREEN = 0xFF0B7A44;
    static final int DARK_GREEN = 0xFF075E3A;
    static final int LIGHT_GREEN = 0xFFEAF6EF;
    static final int YELLOW = 0xFFF4C542;
    static final int SOFT_YELLOW = 0xFFFFF6D6;
    static final int TEXT = 0xFF263238;
    static final int MUTED = 0xFF66746D;
    static final int BG = 0xFFF4F8F6;
    static final int WHITE = 0xFFFFFFFF;

    ArrayList<TimemaniaParser.Draw> draws;
    ExecutorService executor = Executors.newSingleThreadExecutor();

    LinearLayout page;
    TextView baseStatus;
    TextView contestStatus;

    int dp(int v) {
        return (int) (v * getResources().getDisplayMetrics().density + 0.5f);
    }

    GradientDrawable rounded(int color, int radius) {
        GradientDrawable g = new GradientDrawable();
        g.setColor(color);
        g.setCornerRadius(dp(radius));
        return g;
    }

    TextView text(String s, int size, boolean bold, int color) {
        TextView t = new TextView(this);
        t.setText(s);
        t.setTextSize(size);
        t.setTextColor(color);
        t.setPadding(dp(10), dp(8), dp(10), dp(8));
        if (bold) t.setTypeface(Typeface.DEFAULT_BOLD);
        return t;
    }

    Button button(String s, int color) {
        Button b = new Button(this);
        b.setText(s);
        b.setAllCaps(false);
        b.setTextColor(WHITE);
        b.setTextSize(15);
        b.setTypeface(Typeface.DEFAULT_BOLD);
        b.setBackground(rounded(color, 12));
        b.setPadding(dp(10), dp(6), dp(10), dp(6));
        return b;
    }

    LinearLayout card() {
        LinearLayout c = new LinearLayout(this);
        c.setOrientation(LinearLayout.VERTICAL);
        c.setPadding(dp(14), dp(12), dp(14), dp(12));
        c.setBackground(rounded(WHITE, 16));
        c.setElevation(dp(4));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
        lp.setMargins(dp(12), dp(7), dp(12), dp(7));
        c.setLayoutParams(lp);
        return c;
    }

    public void onCreate(Bundle b) {
        super.onCreate(b);
        showHome();
    }

    void showHome() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(BG);

        // Header
        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.VERTICAL);
        header.setPadding(dp(18), dp(18), dp(18), dp(16));
        header.setBackgroundColor(GREEN);

        TextView title = text("☘ MINERADOR TIMEMANIA", 24, true, WHITE);
        TextView sub = text("Histórico • Ciclo • Evolução • Jogo futuro de 10", 13, false, 0xFFE9FFF4);
        header.addView(title);
        header.addView(sub);

        // Quick status strip
        LinearLayout strip = new LinearLayout(this);
        strip.setOrientation(LinearLayout.HORIZONTAL);
        strip.setPadding(0, dp(8), 0, 0);

        baseStatus = text(draws == null ? "BASE: NÃO CARREGADA" : "BASE: " + draws.size() + " CONCURSOS", 12, true, DARK_GREEN);
        baseStatus.setGravity(Gravity.CENTER);
        baseStatus.setBackground(rounded(YELLOW, 10));

        contestStatus = text(draws == null ? "ÚLTIMO: --" : "ÚLTIMO: " + draws.get(draws.size()-1).contest, 12, true, WHITE);
        contestStatus.setGravity(Gravity.CENTER);
        contestStatus.setBackground(rounded(0xFF14945A, 10));

        LinearLayout.LayoutParams half = new LinearLayout.LayoutParams(0, dp(42), 1);
        half.setMargins(0, 0, dp(6), 0);
        strip.addView(baseStatus, half);

        LinearLayout.LayoutParams half2 = new LinearLayout.LayoutParams(0, dp(42), 1);
        half2.setMargins(dp(6), 0, 0, 0);
        strip.addView(contestStatus, half2);

        header.addView(strip);
        root.addView(header);

        ScrollView sv = new ScrollView(this);
        page = new LinearLayout(this);
        page.setOrientation(LinearLayout.VERTICAL);
        page.setPadding(0, dp(7), 0, dp(30));
        sv.addView(page);
        root.addView(sv, new LinearLayout.LayoutParams(-1, 0, 1));

        setContentView(root);

        buildLoadCard();
        buildModule1();
        buildModule2();
        buildModule3();
        buildLegend();
    }

    void buildLoadCard() {
        LinearLayout c = card();
        c.addView(text("📂 BASE DE RESULTADOS", 17, true, DARK_GREEN));
        c.addView(text("Carregue o TXT da Timemania. O aplicativo usa a base inteira para histórico, ciclo e evolução.", 13, false, MUTED));

        Button load = button("CARREGAR TXT DA TIMEMANIA", GREEN);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, dp(58));
        lp.setMargins(0, dp(6), 0, 0);
        c.addView(load, lp);

        load.setOnClickListener(v -> {
            Intent i = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            i.addCategory(Intent.CATEGORY_OPENABLE);
            i.setType("text/*");
            startActivityForResult(i, 7);
        });

        page.addView(c);
    }

    void moduleBadge(LinearLayout c, String no, String title, String desc) {
        LinearLayout line = new LinearLayout(this);
        line.setOrientation(LinearLayout.HORIZONTAL);
        line.setGravity(Gravity.CENTER_VERTICAL);

        TextView badge = text(no, 15, true, DARK_GREEN);
        badge.setGravity(Gravity.CENTER);
        badge.setBackground(rounded(YELLOW, 12));
        line.addView(badge, new LinearLayout.LayoutParams(dp(56), dp(46)));

        LinearLayout labels = new LinearLayout(this);
        labels.setOrientation(LinearLayout.VERTICAL);
        labels.setPadding(dp(10), 0, 0, 0);
        labels.addView(text(title, 18, true, DARK_GREEN));
        labels.addView(text(desc, 12, false, MUTED));
        line.addView(labels, new LinearLayout.LayoutParams(0, -2, 1));

        c.addView(line);
    }

    void buildModule1() {
        LinearLayout c = card();
        moduleBadge(c, "01", "MINERADOR GERAL",
                "Gera um jogo de 10 pela força histórica, últimos 10 concursos e ciclo dos 80 números.");

        LinearLayout mini = new LinearLayout(this);
        mini.setOrientation(LinearLayout.HORIZONTAL);
        mini.setPadding(0, dp(10), 0, dp(8));

        mini.addView(statPill("HISTÓRICO", LIGHT_GREEN), new LinearLayout.LayoutParams(0, dp(44), 1));
        mini.addView(statPill("CICLO", SOFT_YELLOW), new LinearLayout.LayoutParams(0, dp(44), 1));
        mini.addView(statPill("EVOLUÇÃO", 0xFFE7F0FF), new LinearLayout.LayoutParams(0, dp(44), 1));
        c.addView(mini);

        Button run = button("EXECUTAR MINERADOR GERAL", GREEN);
        c.addView(run, new LinearLayout.LayoutParams(-1, dp(58)));

        ProgressBar pb = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        pb.setMax(100);
        LinearLayout.LayoutParams pl = new LinearLayout.LayoutParams(-1, dp(16));
        pl.setMargins(0, dp(8), 0, 0);
        c.addView(pb, pl);

        TextView status = text("Aguardando execução.", 12, true, MUTED);
        c.addView(status);

        LinearLayout result = resultPanel();
        result.setVisibility(View.GONE);
        c.addView(result);

        run.setOnClickListener(v -> {
            if (!checkBase()) return;
            run.setEnabled(false);
            result.setVisibility(View.GONE);
            pb.setProgress(15);
            status.setText("Analisando histórico, últimos 10 e ciclo...");

            executor.submit(() -> {
                try {
                    String rep = MiningEngine.general(draws);
                    runOnUiThread(() -> {
                        pb.setProgress(100);
                        status.setText("Concluído.");
                        showResult(result, "JOGO FUTURO DE 10", rep);
                        run.setEnabled(true);
                    });
                } catch (Exception e) {
                    runOnUiThread(() -> {
                        status.setText("Erro: " + e.getMessage());
                        run.setEnabled(true);
                    });
                }
            });
        });

        page.addView(c);
    }

    TextView statPill(String s, int color) {
        TextView t = text(s, 11, true, DARK_GREEN);
        t.setGravity(Gravity.CENTER);
        t.setBackground(rounded(color, 10));
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(0, dp(44), 1);
        p.setMargins(dp(2), 0, dp(2), 0);
        t.setLayoutParams(p);
        return t;
    }

    void buildModule2() {
        LinearLayout c = card();
        moduleBadge(c, "02", "3 DUPLAS POPULARES",
                "Forma um núcleo de 6 com três duplas fortes e completa 4 dezenas pela evolução.");

        LinearLayout rule = new LinearLayout(this);
        rule.setOrientation(LinearLayout.VERTICAL);
        rule.setPadding(dp(10), dp(8), dp(10), dp(8));
        rule.setBackground(rounded(LIGHT_GREEN, 10));
        rule.addView(text("✓ Núcleo precisa ter no mínimo QUADRA histórica", 12, true, DARK_GREEN));
        rule.addView(text("✓ QUINA e 6 acertos recebem peso maior", 12, true, DARK_GREEN));
        rule.addView(text("✓ As 4 restantes vêm do ciclo/evolução", 12, true, DARK_GREEN));
        c.addView(rule);

        Button run = button("GARIMPAR 3 DUPLAS + EVOLUÇÃO", GREEN);
        LinearLayout.LayoutParams rl = new LinearLayout.LayoutParams(-1, dp(60));
        rl.setMargins(0, dp(8), 0, 0);
        c.addView(run, rl);

        ProgressBar pb = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        pb.setMax(100);
        LinearLayout.LayoutParams pl = new LinearLayout.LayoutParams(-1, dp(16));
        pl.setMargins(0, dp(8), 0, 0);
        c.addView(pb, pl);

        TextView status = text("Aguardando execução.", 12, true, MUTED);
        c.addView(status);

        LinearLayout result = resultPanel();
        result.setVisibility(View.GONE);
        c.addView(result);

        run.setOnClickListener(v -> {
            if (!checkBase()) return;
            run.setEnabled(false);
            result.setVisibility(View.GONE);
            pb.setProgress(0);

            executor.submit(() -> {
                try {
                    String rep = MiningEngine.pairs(draws, (pc, msg) ->
                            runOnUiThread(() -> {
                                pb.setProgress(pc);
                                status.setText(pc + "% • " + msg);
                            })
                    );
                    runOnUiThread(() -> {
                        pb.setProgress(100);
                        status.setText("Concluído.");
                        showResult(result, "NÚCLEO + JOGO FUTURO", rep);
                        run.setEnabled(true);
                    });
                } catch (Exception e) {
                    runOnUiThread(() -> {
                        status.setText("Erro: " + e.getMessage());
                        run.setEnabled(true);
                    });
                }
            });
        });

        page.addView(c);
    }

    void buildModule3() {
        LinearLayout c = card();
        moduleBadge(c, "03", "MINERADOR DE GRUPO",
                "Digite um grupo de 10 a 40 dezenas. O motor extrai 10 somente de dentro dele.");

        EditText input = new EditText(this);
        input.setHint("Ex.: 01 05 08 12 17 23 31 ...");
        input.setMinLines(3);
        input.setGravity(Gravity.TOP);
        input.setTextSize(15);
        input.setTextColor(TEXT);
        input.setHintTextColor(0xFF9AA8A1);
        input.setPadding(dp(12), dp(10), dp(12), dp(10));
        input.setBackground(rounded(0xFFF0F5F2, 12));
        c.addView(input, new LinearLayout.LayoutParams(-1, dp(105)));

        Button run = button("MINERAR GRUPO E GERAR 10", GREEN);
        LinearLayout.LayoutParams rl = new LinearLayout.LayoutParams(-1, dp(58));
        rl.setMargins(0, dp(8), 0, 0);
        c.addView(run, rl);

        LinearLayout result = resultPanel();
        result.setVisibility(View.GONE);
        c.addView(result);

        run.setOnClickListener(v -> {
            if (!checkBase()) return;
            try {
                int[] group = MiningEngine.parse(input.getText().toString());
                String rep = MiningEngine.group(draws, group);
                showResult(result, "EXTRAÇÃO DO GRUPO", rep);
            } catch (Exception e) {
                Toast.makeText(this, "Erro: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });

        page.addView(c);
    }

    void buildLegend() {
        LinearLayout c = card();
        c.setBackground(rounded(0xFFEDF5F1, 16));
        c.addView(text("COMO LER O APLICATIVO", 15, true, DARK_GREEN));
        c.addView(text("🟢 Verde = ação / resultado futuro\n🟡 Amarelo = histórico / referência\n📊 Barra = progresso real do estudo\n📄 Todo resultado pode ser salvo em PDF", 12, false, TEXT));
        page.addView(c);
    }

    LinearLayout resultPanel() {
        LinearLayout r = new LinearLayout(this);
        r.setOrientation(LinearLayout.VERTICAL);
        r.setPadding(dp(12), dp(10), dp(12), dp(10));
        r.setBackground(rounded(0xFFF8FBF9, 12));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
        lp.setMargins(0, dp(10), 0, 0);
        r.setLayoutParams(lp);
        return r;
    }

    void showResult(LinearLayout box, String title, String report) {
        box.removeAllViews();
        box.setVisibility(View.VISIBLE);

        TextView head = text(title, 16, true, DARK_GREEN);
        head.setBackground(rounded(SOFT_YELLOW, 10));
        box.addView(head);

        TextView rep = text(report, 13, false, TEXT);
        rep.setTextIsSelectable(true);
        box.addView(rep);

        Button pdf = button("GERAR PDF DO RESULTADO", DARK_GREEN);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, dp(54));
        lp.setMargins(0, dp(8), 0, 0);
        box.addView(pdf, lp);
        pdf.setOnClickListener(v -> savePdf(title, report));
    }

    boolean checkBase() {
        if (draws == null) {
            Toast.makeText(this, "Carregue primeiro o TXT da Timemania.", Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    protected void onActivityResult(int req, int res, Intent data) {
        super.onActivityResult(req, res, data);
        if (req == 7 && res == RESULT_OK && data != null && data.getData() != null) {
            try (InputStream in = getContentResolver().openInputStream(data.getData())) {
                draws = TimemaniaParser.parse(in);
                TimemaniaParser.Draw last = draws.get(draws.size() - 1);
                Toast.makeText(this,
                        "Base carregada: " + draws.size() + " concursos • último " + last.contest,
                        Toast.LENGTH_LONG).show();
                showHome();
            } catch (Exception e) {
                Toast.makeText(this, "Erro: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }

    void savePdf(String title, String report) {
        try {
            PdfDocument doc = new PdfDocument();
            ArrayList<String> lines = wrap(report, 82);
            int at = 0, pageNo = 1;

            while (at < lines.size()) {
                PdfDocument.Page pagePdf = doc.startPage(
                        new PdfDocument.PageInfo.Builder(595, 842, pageNo++).create());

                Canvas c = pagePdf.getCanvas();
                Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);

                p.setColor(GREEN);
                c.drawRect(0, 0, 595, 84, p);

                p.setColor(WHITE);
                p.setTypeface(Typeface.DEFAULT_BOLD);
                p.setTextSize(18);
                c.drawText("MINERADOR TIMEMANIA", 26, 36, p);

                p.setTextSize(11);
                c.drawText(title, 26, 60, p);

                p.setColor(TEXT);
                p.setTypeface(Typeface.DEFAULT);
                p.setTextSize(9);

                int y = 110;
                while (at < lines.size() && y < 805) {
                    c.drawText(lines.get(at++), 26, y, p);
                    y += 13;
                }

                doc.finishPage(pagePdf);
            }

            String name = "MINERADOR_TIMEMANIA_" + System.currentTimeMillis() + ".pdf";
            ContentValues values = new ContentValues();
            values.put(MediaStore.Downloads.DISPLAY_NAME, name);
            values.put(MediaStore.Downloads.MIME_TYPE, "application/pdf");
            values.put(MediaStore.Downloads.RELATIVE_PATH, "Download/MINERADOR_TIMEMANIA");

            Uri uri = getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);
            OutputStream out = getContentResolver().openOutputStream(uri);
            doc.writeTo(out);
            out.close();
            doc.close();

            Toast.makeText(this, "PDF salvo em Download/MINERADOR_TIMEMANIA.", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(this, "Erro ao gerar PDF: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    ArrayList<String> wrap(String text, int max) {
        ArrayList<String> out = new ArrayList<>();
        for (String line : text.split("\n", -1)) {
            String s = line;
            if (s.isEmpty()) {
                out.add("");
                continue;
            }
            while (s.length() > max) {
                int cut = s.lastIndexOf(' ', max);
                if (cut < 15) cut = max;
                out.add(s.substring(0, cut));
                s = s.substring(cut).trim();
            }
            out.add(s);
        }
        return out;
    }
}
