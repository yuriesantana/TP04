
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.text.NumberFormat;
import java.util.Locale;
import javax.swing.*;

public class App extends JFrame {

    // Conexao com o banco
    private Connection conexao;

    // Consulta SQL
    private PreparedStatement pst;
    private ResultSet rs;

    // Componentes da tela
    private JTextField txtPesquisa;
    private JTextField txtNome;
    private JTextField txtSalario;
    private JTextField txtCargo;

    private JButton btnPesquisar;
    private JButton btnAnterior;
    private JButton btnProximo;

    // Construtor
    public App() {

        setTitle("Consulta de Funcionarios");
        setSize(520, 360);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        criarInterface();
        conectarBanco();

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                fecharConexao();
                dispose();
            }
        });

        setVisible(true);
    }

    // Criacao da interface
    private void criarInterface() {

        JPanel painel = new JPanel(new GridBagLayout());
        painel.setBorder(
            BorderFactory.createEmptyBorder(20, 25, 20, 25)
        );

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(7, 7, 7, 7);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel titulo = new JLabel(
            "CONSULTA DE FUNCIONARIOS",
            SwingConstants.CENTER
        );

        titulo.setFont(new Font("Arial", Font.BOLD, 19));

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 3;
        painel.add(titulo, gbc);

        // Pesquisa
        gbc.gridwidth = 1;

        gbc.gridx = 0;
        gbc.gridy = 1;
        painel.add(new JLabel("Pesquisar nome:"), gbc);

        txtPesquisa = new JTextField(20);

        gbc.gridx = 1;
        gbc.gridwidth = 2;
        painel.add(txtPesquisa, gbc);

        // Botao pesquisar
        btnPesquisar = new JButton("Pesquisar");

        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        painel.add(btnPesquisar, gbc);

        // Nome
        gbc.gridwidth = 1;
        gbc.gridx = 0;
        gbc.gridy = 3;
        painel.add(new JLabel("Nome:"), gbc);

        txtNome = new JTextField(25);
        txtNome.setEditable(false);

        gbc.gridx = 1;
        gbc.gridwidth = 2;
        painel.add(txtNome, gbc);

        // Salario
        gbc.gridwidth = 1;
        gbc.gridx = 0;
        gbc.gridy = 4;
        painel.add(new JLabel("Salario:"), gbc);

        txtSalario = new JTextField(25);
        txtSalario.setEditable(false);

        gbc.gridx = 1;
        gbc.gridwidth = 2;
        painel.add(txtSalario, gbc);

        // Cargo
        gbc.gridwidth = 1;
        gbc.gridx = 0;
        gbc.gridy = 5;
        painel.add(new JLabel("Cargo:"), gbc);

        txtCargo = new JTextField(25);
        txtCargo.setEditable(false);

        gbc.gridx = 1;
        gbc.gridwidth = 2;
        painel.add(txtCargo, gbc);

        // Botoes de navegacao
        btnAnterior = new JButton("Anterior");
        btnProximo = new JButton("Proximo");

        btnAnterior.setEnabled(false);
        btnProximo.setEnabled(false);

        gbc.gridwidth = 1;
        gbc.gridx = 0;
        gbc.gridy = 6;
        painel.add(btnAnterior, gbc);

        gbc.gridx = 1;
        gbc.gridwidth = 2;
        painel.add(btnProximo, gbc);

        add(painel);

        // Eventos
        btnPesquisar.addActionListener(e -> pesquisar());

        btnAnterior.addActionListener(e -> anterior());

        btnProximo.addActionListener(e -> proximo());

        txtPesquisa.addActionListener(e -> pesquisar());
    }

    // Conexao JDBC
    private void conectarBanco() {

        String url = "jdbc:sqlserver://localhost:14330;"
                + "databaseName=aulajava;"
                + "integratedSecurity=true;"
                + "encrypt=true;"
                + "trustServerCertificate=true;";

        try {

            Class.forName(
                "com.microsoft.sqlserver.jdbc.SQLServerDriver"
            );

            conexao = DriverManager.getConnection(url);

            System.out.println(
                "CONEXAO COM SQL SERVER REALIZADA!"
            );

        } catch (ClassNotFoundException | SQLException erro) {

            JOptionPane.showMessageDialog(
                this,
                "Erro na conexao: " + erro.getMessage(),
                "Erro",
                JOptionPane.ERROR_MESSAGE
            );

            System.out.println(erro.getMessage());
        }
    }

    // Pesquisa de funcionarios
    private void pesquisar() {

        try {

            if (conexao == null || conexao.isClosed()) {
                JOptionPane.showMessageDialog(
                    this,
                    "Banco de dados nao conectado!"
                );
                return;
            }

            fecharConsulta();

            String sql =
                "SELECT f.nome_func, f.sal_func, c.ds_cargo " +
                "FROM tbfuncs f " +
                "INNER JOIN tbcargos c " +
                "ON f.cod_cargo = c.cd_cargo " +
                "WHERE f.nome_func LIKE ? " +
                "ORDER BY f.cod_func";

            pst = conexao.prepareStatement(
                sql,
                ResultSet.TYPE_SCROLL_INSENSITIVE,
                ResultSet.CONCUR_READ_ONLY
            );

            pst.setString(
                1,
                "%" + txtPesquisa.getText().trim() + "%"
            );

            rs = pst.executeQuery();

            if (rs.next()) {

                mostrarRegistro();
                atualizarBotoes();

            } else {

                limparCampos();

                btnAnterior.setEnabled(false);
                btnProximo.setEnabled(false);

                JOptionPane.showMessageDialog(
                    this,
                    "Nenhum funcionario encontrado!"
                );
            }

        } catch (SQLException erro) {

            JOptionPane.showMessageDialog(
                this,
                "Erro na pesquisa: " + erro.getMessage()
            );
        }
    }

    // Mostrar os dados na tela
    private void mostrarRegistro() throws SQLException {

        txtNome.setText(
            rs.getString("nome_func").trim()
        );

        NumberFormat formato = NumberFormat.getCurrencyInstance(
            new Locale("pt", "BR")
        );

        java.math.BigDecimal salario =
            rs.getBigDecimal("sal_func");

        txtSalario.setText(
            salario == null ? "" : formato.format(salario)
        );

        String cargo = rs.getString("ds_cargo");

        txtCargo.setText(
            cargo == null ? "" : cargo.trim()
        );
    }

    // Proximo funcionario
    private void proximo() {

        try {

            if (rs != null && rs.next()) {

                mostrarRegistro();
                atualizarBotoes();
            }

        } catch (SQLException erro) {

            JOptionPane.showMessageDialog(
                this,
                "Erro ao avancar: " + erro.getMessage()
            );
        }
    }

    // Funcionario anterior
    private void anterior() {

        try {

            if (rs != null && rs.previous()) {

                mostrarRegistro();
                atualizarBotoes();
            }

        } catch (SQLException erro) {

            JOptionPane.showMessageDialog(
                this,
                "Erro ao voltar: " + erro.getMessage()
            );
        }
    }

    // Controle dos botoes
    private void atualizarBotoes() throws SQLException {

        btnAnterior.setEnabled(!rs.isFirst());
        btnProximo.setEnabled(!rs.isLast());
    }

    // Limpar campos
    private void limparCampos() {

        txtNome.setText("");
        txtSalario.setText("");
        txtCargo.setText("");
    }

    // Fechar consulta
    private void fecharConsulta() {

        try {

            if (rs != null) {
                rs.close();
                rs = null;
            }

            if (pst != null) {
                pst.close();
                pst = null;
            }

        } catch (SQLException erro) {

            System.out.println(erro.getMessage());
        }
    }

    // Fechar conexao
    private void fecharConexao() {

        fecharConsulta();

        try {

            if (conexao != null && !conexao.isClosed()) {
                conexao.close();
                System.out.println("Conexao encerrada!");
            }

        } catch (SQLException erro) {

            System.out.println(erro.getMessage());
        }
    }

    // Metodo principal
    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {
            new App();
        });
    }
}
