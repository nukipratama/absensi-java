package rfidreader;

import java.awt.*;
import java.io.*;
import static java.lang.Thread.sleep;
import java.sql.*;
import java.text.*;
import java.time.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;

public class tapClass extends javax.swing.JFrame {

    //instansiasi variable
    //--------------------------------------------------------------------start
    LocalDateTime now = LocalDateTime.now();
    koneksiDB koneksi = new koneksiDB();
    private int hour, mins;
    private final boolean ulang = true;
    private DefaultTableModel tabelLayout;
    private String dosen = "";
    private ResultSet rs;
    private int matkulPilih = 0;
    DateFormat dateLong = new SimpleDateFormat("EE dd/MM/yyy - HH:mm:ss");
    DateFormat df = new SimpleDateFormat("dd/MM/yyy - HH:mm:ss");
    DateFormat hourString = new SimpleDateFormat("HH");
    DateFormat minsString = new SimpleDateFormat("mm");
    //--------------------------------------------------------------------end

    //Check id RFID dan Fingerprint ke Database
    //--------------------------------------------------------------------start
    public boolean check(String id) throws SQLException {
        boolean benar = false;
        ResultSet rs = koneksi.DATA("select * from rfidtb where id='" + id + "'");
        if (rs.next()) {
            benar = true;
        }
        return benar;
    }
    //--------------------------------------------------------------------end

    //Authenticator untuk Dosen
    //--------------------------------------------------------------------start
    public boolean checkDosen(String id) throws SQLException {
        boolean benar = false;
        dosen = JOptionPane.showInputDialog("Masukkan ID dosen");
        rs = koneksi.DATA("select * from rfidtb where id='" + dosen + "' and jabatan='Dosen'");
        if (rs.next()) {
            benar = true;
        }
        return benar;
    }
    //--------------------------------------------------------------------end

    //penyortiran database untuk menampilkan status tapping kelas
    //--------------------------------------------------------------------start
    public boolean tabelDosen(String id) throws SQLException {

        boolean benar = false;
        dosen = inputField.getText();
        rs = koneksi.DATA("select * from rfidtb where id='" + dosen + "' and jabatan='Dosen'");
        if (rs.next()) {
            benar = true;
        }
        return benar;
    }
    //--------------------------------------------------------------------end

    //agar menampilkan Jam dinamis
    //--------------------------------------------------------------------start
    public void clock() {
        Thread clock = new Thread() {
            @Override
            public void run() {
                try {
                    while (ulang == true) {
                        Calendar cl = Calendar.getInstance();
                        jamField.setText("Date/Time : " + dateLong.format(cl.getTime()));
                        sleep(1000);
                    }
                } catch (InterruptedException e) {
                }

            }
        };
        clock.start();
    }
    //--------------------------------------------------------------------end

    //agar menampilkan Judul GUI
    //--------------------------------------------------------------------start
    public void title() {
        Thread clock = new Thread() {
            @Override
            public void run() {
                try {
                    while (ulang == true) {
                        Calendar cl = Calendar.getInstance();
                        setTitle("RFID & Fingerprint Scanner | " + dateLong.format(cl.getTime()));
                        sleep(1000);
                    }
                } catch (InterruptedException e) {
                }

            }
        };
        clock.start();
    }
    //--------------------------------------------------------------------end

    //Constructor
    //--------------------------------------------------------------------start
    public tapClass() {
        initComponents();
        logTable.setVisible(false);
        clock();
        title();
        setResizable(false);
        ImageIcon icon = new ImageIcon("E:\\Documents\\NetBeansProjects\\RFIDreader\\src\\rfidreader\\photolib\\logofri.png");
        Image image = icon.getImage();
        Image newimg = image.getScaledInstance(logoFri.getWidth(), logoFri.getHeight(), Image.SCALE_SMOOTH);
        icon = new ImageIcon(newimg);
        logoFri.setIcon(icon);
        javax.swing.border.Border border = LineBorder.createGrayLineBorder();
        fotoField.setBorder(border);
    }
    //--------------------------------------------------------------------end

    //Penghitung total absensi berdasarkan id dan matkul
    //--------------------------------------------------------------------start
    public double totalHadir(String idnya, String matkul) {
        double totalRow = 0;
        try {

            String query = "select count(*) from log where id='" + idnya + "' and matkul='" + matkul + "'  ";
            ResultSet rs = koneksi.DATA(query);
            while (rs.next()) {
                totalRow = rs.getInt("count(*)");
            }
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
        return totalRow;
    }
    //--------------------------------------------------------------------end

    //penghitung total absensi yang diterima berdasarkan matkul
    //--------------------------------------------------------------------start
    public double totalAccepted(String idnya, String matkul) {
        double acceptedRow = 0;
        try {

            String query = "select count(*) from log where id='" + idnya + "' and status='Diterima' and matkul ='" + matkul + "'";
            ResultSet rs = koneksi.DATA(query);
            while (rs.next()) {
                acceptedRow = rs.getInt("count(*)");
            }
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
        return acceptedRow;
    }
    //--------------------------------------------------------------------end

    //untuk Dosen, menampilkan status Absensi Kelas, siapa mahasiswa/i yang sudah tapping
    //--------------------------------------------------------------------start
    public void tabelShow(String matkul) {
        tabelLayout();
        logTable.setVisible(true);
        tabelLayout.getDataVector().removeAllElements();
        tabelLayout.fireTableDataChanged();
        try {
            ResultSet tabel = koneksi.DATA("SELECT * FROM presensi where matkul='" + matkul + "'");

            while (tabel.next()) {
                Object[] o = new Object[6];
                o[0] = tabel.getString("nama");
                o[1] = tabel.getString("id");
                o[2] = tabel.getString("matkul");
                o[3] = tabel.getString("ruangan");
                o[4] = tabel.getString("jam");
                o[5] = tabel.getString("media");

                tabelLayout.addRow(o);
            }

        } catch (SQLException e) {
            System.out.println("Tabel Show Gagal.");
        }
    }
    //--------------------------------------------------------------------end

    //untuk menampilkan riwayat absensi mahasiswa
    //--------------------------------------------------------------------start
    public void tabelShowMhs(String rfid, String matkul) {
        tabelLayout();
        tabelLayout.addColumn("Status");
        logTable.setVisible(true);

        try {
            ResultSet tabel = koneksi.DATA("SELECT * FROM log where id='" + rfid + "' and matkul ='" + matkul + "'");
            while (tabel.next()) {
                Object[] o = new Object[7];
                o[0] = tabel.getString("nama");
                o[1] = tabel.getString("id");
                o[2] = tabel.getString("matkul");
                o[3] = tabel.getString("ruangan");
                o[4] = tabel.getString("jam");
                o[5] = tabel.getString("media");
                o[6] = tabel.getString("status");
                tabelLayout.addRow(o);
            }
            double downloaded = totalAccepted(rfid, matkul);
            double total = totalHadir(rfid, matkul);
            double percent = (100 * downloaded) / total;
            persentaseField.setText(String.format("Persentasi Kehadiran : " + "%.0f%%", percent));
        } catch (Exception e) {
            System.out.println("Tabel Show Gagal.");
        }
    }
    //--------------------------------------------------------------------end

    //untuk mengatur Column pada tabel
    //--------------------------------------------------------------------start
    public void tabelLayout() {
        tabelLayout = new DefaultTableModel();
        tabelLayout.getDataVector().removeAllElements();
        tabelLayout.fireTableDataChanged();
        logTable.setModel(tabelLayout);
        tabelLayout.addColumn("Nama");
        tabelLayout.addColumn("NIM");
        tabelLayout.addColumn("Mata Kuliah");
        tabelLayout.addColumn("Ruangan");
        tabelLayout.addColumn("Tanggal");
        tabelLayout.addColumn("Media");

    }
    //--------------------------------------------------------------------end

    //untuk menginput absensi ke database
    //--------------------------------------------------------------------start
    public void tap(String media, String matkul, String metode, String ruangan) {

        Calendar cl = Calendar.getInstance();
        hour = Integer.parseInt(hourString.format(cl.getTime()));
        mins = Integer.parseInt(minsString.format(cl.getTime()));
        String rfid = inputField.getText();

        try {
            if (check(rfid)) {
                String sql = "SELECT * FROM rfidtb WHERE id='" + inputField.getText() + "'";
                ResultSet rs = koneksi.DATA(sql);
                if (rs.next()) {
                    String nama = rs.getString("nama");
                    String jam = df.format(cl.getTime());
                    Statement stm = (Statement) koneksi.koneksi.createStatement();
                    stm.executeUpdate("INSERT INTO presensi VALUES ('" + nama + "','" + rfid + "','" + matkul + "','" + ruangan + "','" + jam + "','" + media + "')");
                    JOptionPane.showMessageDialog(this, metode + " identification berhasil.");
                }
            } else {
                JOptionPane.showMessageDialog(null, "ID Salah", "Access Denied", JOptionPane.ERROR_MESSAGE);
            }
        } catch (HeadlessException | SQLException ex) {
            JOptionPane.showMessageDialog(null, "Anda telah tapping.");
        }

    }
    //--------------------------------------------------------------------end

    //untuk menampilkan foto
    //--------------------------------------------------------------------start
    public void showFoto(String rfid) {
        String namafile = "D:\\" + rfid + ".jpg";
        try {
            Class.forName("com.mysql.jdbc.Driver");
            Connection con = DriverManager.getConnection("jdbc:mysql://85.10.205.173:3307/nukipratama", "nukidb", "nukidb");
            File file = new File(namafile);
            FileOutputStream fos = new FileOutputStream(file);
            byte b[];
            Blob blob;
            PreparedStatement ps = con.prepareStatement("select * from rfidtb where id='" + rfid + "'");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                blob = rs.getBlob("foto");
                b = blob.getBytes(1, (int) blob.length());
                fos.write(b);
                ImageIcon icon = new ImageIcon(namafile);
                Image image = icon.getImage();
                Image newimg = image.getScaledInstance(fotoField.getWidth(), fotoField.getHeight(), Image.SCALE_SMOOTH);
                icon = new ImageIcon(newimg);
                fotoField.setIcon(icon);
            }
            ps.close();
            fos.close();
            con.close();
        } catch (IOException | ClassNotFoundException | SQLException e) {
            fotoField.setText("Tidak ada foto.");
        }

    }
    //--------------------------------------------------------------------end

    //untuk mengisi method dari tombol details
    //--------------------------------------------------------------------start
    public void detailsPanel() {

        boolean test = true;
        String rfid = inputField.getText();

        try {
            Class.forName("com.mysql.jdbc.Driver");
            Connection con = DriverManager.getConnection("jdbc:mysql://85.10.205.173:3307/nukipratama", "nukidb", "nukidb");

            PreparedStatement ps = con.prepareStatement("select * from rfidtb where id='" + rfid + "'");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String nama = rs.getString("nama");
                String id = rs.getString("id");
                String jurusan = rs.getString("jurusan");
                String jabatan = rs.getString("jabatan");
                //===============================================//
                byte b[];
                Blob blob;
                blob = rs.getBlob("foto");
                b = blob.getBytes(1, (int) blob.length());

                //===============================================//
                String namafile = "D:\\" + rfid + ".jpg";
                File file = new File(namafile);
                FileOutputStream fos = new FileOutputStream(file);
                fos.write(b);

                detailsPanel dp = new detailsPanel();
                ImageIcon icon = new ImageIcon(namafile);
                Image image = icon.getImage();
                Image newimg = image.getScaledInstance(fotoField.getWidth(), fotoField.getHeight(), Image.SCALE_SMOOTH);
                icon = new ImageIcon(newimg);
                fotoField.setIcon(icon);
                dp.ambilData(nama, id, jurusan, jabatan, namafile);
                dp.setVisible(true);
            }

        } catch (IOException | ClassNotFoundException | SQLException e) {
            fotoField.setText("Tidak ada foto.");
        }
    }
    //--------------------------------------------------------------------end

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jMenu3 = new javax.swing.JMenu();
        buttonGroup1 = new javax.swing.ButtonGroup();
        buttonGroup2 = new javax.swing.ButtonGroup();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        inputField = new javax.swing.JTextField();
        tapButton = new javax.swing.JButton();
        jamField = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        logTable = new javax.swing.JTable();
        jLabel2 = new javax.swing.JLabel();
        persentaseField = new javax.swing.JLabel();
        fingerButton = new javax.swing.JButton();
        checkButton = new javax.swing.JButton();
        jRadioButton1 = new javax.swing.JRadioButton();
        jRadioButton2 = new javax.swing.JRadioButton();
        jLabel3 = new javax.swing.JLabel();
        fotoField = new javax.swing.JLabel();
        detailsButton = new javax.swing.JButton();
        logoFri = new javax.swing.JLabel();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu4 = new javax.swing.JMenu();
        jMenuItem1 = new javax.swing.JMenuItem();
        jMenuItem2 = new javax.swing.JMenuItem();
        jMenu1 = new javax.swing.JMenu();
        jMenu2 = new javax.swing.JMenu();

        jMenu3.setText("jMenu3");

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel1.setPreferredSize(new java.awt.Dimension(907, 561));

        jLabel1.setFont(new java.awt.Font("Dialog", 0, 14)); // NOI18N
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel1.setText("RFID / Fingerprint ID");

        tapButton.setText("RFID");
        tapButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tapButtonActionPerformed(evt);
            }
        });

        jamField.setFont(new java.awt.Font("Dialog", 0, 14)); // NOI18N
        jamField.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);

        logTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        jScrollPane1.setViewportView(logTable);

        jLabel2.setFont(new java.awt.Font("Dialog", 0, 18)); // NOI18N
        jLabel2.setText("Status Kehadiran Mahasiswa");

        persentaseField.setFont(new java.awt.Font("Dialog", 1, 18)); // NOI18N
        persentaseField.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        persentaseField.setText("Persentasi Kehadiran : ");

        fingerButton.setText("Fingerprint");
        fingerButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fingerButtonActionPerformed(evt);
            }
        });

        checkButton.setText("Check");
        checkButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkButtonActionPerformed(evt);
            }
        });

        buttonGroup1.add(jRadioButton1);
        jRadioButton1.setFont(new java.awt.Font("Dialog", 0, 14)); // NOI18N
        jRadioButton1.setText("Object Oriented Programming (13.00 - 23.59)");
        jRadioButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButton1ActionPerformed(evt);
            }
        });

        buttonGroup1.add(jRadioButton2);
        jRadioButton2.setFont(new java.awt.Font("Dialog", 0, 14)); // NOI18N
        jRadioButton2.setText("Data Structure (00.00 - 12.59)");
        jRadioButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButton2ActionPerformed(evt);
            }
        });

        jLabel3.setFont(new java.awt.Font("Dialog", 0, 14)); // NOI18N
        jLabel3.setText("Mata Kuliah : ");

        fotoField.setBackground(new java.awt.Color(51, 51, 51));
        fotoField.setForeground(new java.awt.Color(0, 0, 0));
        fotoField.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        fotoField.setText("Foto");

        detailsButton.setText("Details");
        detailsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                detailsButtonActionPerformed(evt);
            }
        });

        logoFri.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(27, 27, 27)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(402, 402, 402)
                        .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 263, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(452, Short.MAX_VALUE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jRadioButton1)
                                    .addComponent(jRadioButton2)
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addGap(25, 25, 25)
                                        .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 86, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addComponent(logoFri, javax.swing.GroupLayout.PREFERRED_SIZE, 240, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                                            .addGap(257, 257, 257)
                                            .addComponent(persentaseField, javax.swing.GroupLayout.PREFERRED_SIZE, 289, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addGroup(jPanel1Layout.createSequentialGroup()
                                            .addComponent(jLabel1)
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                            .addComponent(inputField, javax.swing.GroupLayout.PREFERRED_SIZE, 191, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                                .addComponent(fingerButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                .addComponent(tapButton, javax.swing.GroupLayout.PREFERRED_SIZE, 95, javax.swing.GroupLayout.PREFERRED_SIZE))
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                                .addComponent(checkButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                .addComponent(detailsButton, javax.swing.GroupLayout.PREFERRED_SIZE, 95, javax.swing.GroupLayout.PREFERRED_SIZE))))
                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                                        .addComponent(jamField, javax.swing.GroupLayout.PREFERRED_SIZE, 310, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(172, 172, 172)))
                                .addGap(18, 18, 18)
                                .addComponent(fotoField, javax.swing.GroupLayout.PREFERRED_SIZE, 165, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jScrollPane1))
                        .addGap(27, 27, 27))))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(53, 53, 53)
                        .addComponent(jamField, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(14, 14, 14)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(inputField, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 62, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(jLabel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 62, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(tapButton, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(checkButton, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(fingerButton, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(detailsButton, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE))))
                        .addGap(18, 18, 18)
                        .addComponent(persentaseField, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(32, 32, 32)
                        .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(26, 26, 26)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(logoFri, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jRadioButton1)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jRadioButton2))
                            .addComponent(fotoField, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 303, Short.MAX_VALUE)
                .addGap(9, 9, 9))
        );

        jMenu4.setText("Dosen");
        jMenu4.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N

        jMenuItem1.setText("Delete");
        jMenuItem1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem1ActionPerformed(evt);
            }
        });
        jMenu4.add(jMenuItem1);

        jMenuItem2.setText("Input");
        jMenuItem2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem2ActionPerformed(evt);
            }
        });
        jMenu4.add(jMenuItem2);

        jMenuBar1.add(jMenu4);

        jMenu1.setText("Pendaftaran");
        jMenu1.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        jMenu1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jMenu1MouseClicked(evt);
            }
        });
        jMenuBar1.add(jMenu1);

        jMenu2.setText("Exit");
        jMenu2.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        jMenu2.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jMenu2MouseClicked(evt);
            }
        });
        jMenuBar1.add(jMenu2);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, 1144, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 577, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    //listener untuk RFID button
    //--------------------------------------------------------------------start
    private void tapButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tapButtonActionPerformed
        Calendar cl = Calendar.getInstance();
        hour = Integer.parseInt(hourString.format(cl.getTime()));
        mins = Integer.parseInt(minsString.format(cl.getTime()));
        String input = inputField.getText();
        serahla();
        switch (matkulPilih) {
            case 0:
                JOptionPane.showMessageDialog(this, "Silahkan pilih Mata Kuliah", "Access Denied", JOptionPane.ERROR_MESSAGE);
                break;
            case 1:
                if (hour >= 13 && mins >= 00 && hour <= 23 && mins <= 59) {
                    tap("RFID", "Object Oriented Programming", "RFID", "B205");
                    showFoto(input);
                } else {
                    JOptionPane.showMessageDialog(this, "Tidak ada Jadwal untuk Mata Kuliah yang dipilih", "Tapping Ditolak", JOptionPane.ERROR_MESSAGE);
                }
                break;
            case 2:
                if (hour >= 00 && mins >= 00 && hour <= 12 && mins <= 59) {
                    tap("RFID", "Data Structure", "RFID", "B210");
                    showFoto(input);
                } else {
                    JOptionPane.showMessageDialog(this, "Tidak ada Jadwal untuk Mata Kuliah yang dipilih", "Tapping Ditolak", JOptionPane.ERROR_MESSAGE);
                }
                break;
        }

    }//GEN-LAST:event_tapButtonActionPerformed
    //--------------------------------------------------------------------end

    //listener untuk fingerprint button
    //--------------------------------------------------------------------start
    private void fingerButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fingerButtonActionPerformed
        Calendar cl = Calendar.getInstance();
        hour = Integer.parseInt(hourString.format(cl.getTime()));
        mins = Integer.parseInt(minsString.format(cl.getTime()));
        String input = inputField.getText();
        serahla();
        switch (matkulPilih) {
            case 0:
                JOptionPane.showMessageDialog(this, "Silahkan pilih Mata Kuliah", "Access Denied", JOptionPane.ERROR_MESSAGE);
                break;
            case 1:
                if (hour >= 13 && mins >= 00 && hour <= 23 && mins <= 59) {
                    tap("Fingerprint", "Object Oriented Programming", "Fingerprint", "B205");
                    showFoto(input);
                } else {
                    JOptionPane.showMessageDialog(this, "Tidak ada Jadwal untuk Mata Kuliah yang dipilih", "Tapping Ditolak", JOptionPane.ERROR_MESSAGE);
                }
                break;
            case 2:
                if (hour >= 00 && mins >= 00 && hour <= 12 && mins <= 59) {
                    tap("Fingerprint", "Data Structure", "Fingerprint", "B210");
                    showFoto(input);
                } else {
                    JOptionPane.showMessageDialog(this, "Tidak ada Jadwal untuk Mata Kuliah yang dipilih", "Tapping Ditolak", JOptionPane.ERROR_MESSAGE);
                }
                break;
        }

    }//GEN-LAST:event_fingerButtonActionPerformed
    //--------------------------------------------------------------------end

    //kalau tidak ada matkul dipilih, mengembalikan matkulPilih ke 0
    //--------------------------------------------------------------------start
    public void serahla() {
        boolean tes = jRadioButton1.isSelected();
        boolean tos = jRadioButton2.isSelected();
        if (tes == false && tos == false) {
            matkulPilih = 0;
        }
    }
    //--------------------------------------------------------------------end

    //listener untuk check button
    //--------------------------------------------------------------------start
    private void checkButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_checkButtonActionPerformed
        String input = inputField.getText();
        persentaseField.setText("Persentasi Kehadiran : ");
        serahla();
        try {
            if (tabelDosen(input)) {
                try {
                    switch (matkulPilih) {
                        case 0:
                            JOptionPane.showMessageDialog(this, "Silahkan pilih Mata Kuliah", "Access Denied", JOptionPane.ERROR_MESSAGE);
                            break;
                        case 1:
                            tabelShow("Object Oriented Programming");
                            showFoto(input);
                            break;
                        case 2:
                            tabelShow("Data Structure");
                            showFoto(input);
                            break;
                    }

                } catch (HeadlessException ex) {
                    JOptionPane.showMessageDialog(null, "Gagal Koneksi ke Database.");
                }

            } else if (check(input)) {
                switch (matkulPilih) {
                    case 0:
                        JOptionPane.showMessageDialog(this, "Silahkan pilih Mata Kuliah", "Access Denied", JOptionPane.ERROR_MESSAGE);
                        break;
                    case 1:
                        tabelShowMhs(input, "Object Oriented Programming");
                        showFoto(input);
                        break;
                    case 2:
                        tabelShowMhs(input, "Data Structure");
                        showFoto(input);
                        break;
                }
            } else {
                JOptionPane.showMessageDialog(this, "ID Salah", "Access Denied", JOptionPane.ERROR_MESSAGE);
            }
        } catch (HeadlessException | SQLException e) {

        }

    }//GEN-LAST:event_checkButtonActionPerformed
    //--------------------------------------------------------------------end

    //untuk DOSEN, agar absensi kelas yang sudah fix dipindahkan ke tabel lain, agar mahasiswa/i bisa tau status presensinya yg up-to-date
    //--------------------------------------------------------------------start
    private void jMenuItem2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem2ActionPerformed

        try {
            if (checkDosen(dosen)) {
                int jawab = JOptionPane.showConfirmDialog(this, "Apakah absensi sudah dilakukan ?");
                String sql = "SELECT * FROM presensi";
                rs = koneksi.DATA(sql);
                switch (jawab) {
                    case JOptionPane.YES_OPTION:
                        while (rs.next()) {
                            String nama = rs.getString("nama");
                            String matkul = rs.getString("matkul");
                            String id = rs.getString("id");
                            String jam = rs.getString("jam");
                            String media = rs.getString("media");
                            String ruangan = rs.getString("ruangan");
                            String status = "Diterima";
                            Statement stm = (Statement) koneksi.koneksi.createStatement();
                            stm.executeUpdate("INSERT INTO log VALUES ('" + nama + "','" + id + "','" + matkul + "','" + ruangan + "','" + jam + "','" + media + "','" + status + "')");
                            stm.executeUpdate("DELETE FROM presensi where id='" + id + "'");
                        }
                        JOptionPane.showMessageDialog(null, "Input berhasil.");
                        break;
                }
            } else {
                JOptionPane.showMessageDialog(null, "ID dosen salah.");
            }
        } catch (HeadlessException | SQLException ex) {
            JOptionPane.showMessageDialog(null, "Gagal Koneksi ke Database.");
        }
    }//GEN-LAST:event_jMenuItem2ActionPerformed
    //--------------------------------------------------------------------end

    //untuk DOSEN, agar membatalkan kehadiran mahasiswa/i yg bermasalah di kelas
    //--------------------------------------------------------------------start
    private void jMenuItem1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem1ActionPerformed
        try {
            if (checkDosen(dosen)) {
                String refusedID = JOptionPane.showInputDialog("Masukkan ID mahasiswa/i yang akan ditolak");
                String sql = "SELECT * FROM presensi where id='" + refusedID + "'";
                rs = koneksi.DATA(sql);
                while (rs.next()) {
                    String nama = rs.getString("nama");
                    String matkul = rs.getString("matkul");
                    String jam = rs.getString("jam");
                    String media = rs.getString("media");
                    String ruangan = rs.getString("ruangan");
                    String status = "Ditolak";
                    Statement stm = (Statement) koneksi.koneksi.createStatement();
                    stm.executeUpdate("INSERT INTO log VALUES ('" + nama + "','" + refusedID + "','" + matkul + "','" + ruangan + "','" + jam + "','" + media + "','" + status + "')");
                    stm.executeUpdate("DELETE FROM presensi where id='" + refusedID + "'");

                }
                JOptionPane.showMessageDialog(this, "Berhasil");
            } else {
                JOptionPane.showMessageDialog(this, "ID dosen salah.");
            }
        } catch (HeadlessException | SQLException ex) {
            JOptionPane.showMessageDialog(null, "Gagal Koneksi ke Database.");
        }

    }//GEN-LAST:event_jMenuItem1ActionPerformed
    //--------------------------------------------------------------------end

    //untuk membuka panel Admin, untuk mendaftarkan Dosen/Mahasiswa
    //--------------------------------------------------------------------start
    private void jMenu1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jMenu1MouseClicked
        new Admin().setVisible(true);
        this.dispose();
    }//GEN-LAST:event_jMenu1MouseClicked
    //--------------------------------------------------------------------end

    //fungsi ketika matkul OOP ditekan
    //--------------------------------------------------------------------start
    private void jRadioButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButton1ActionPerformed
        jRadioButton2.setSelected(false);
        jRadioButton1.isSelected();
        matkulPilih = 1;

    }//GEN-LAST:event_jRadioButton1ActionPerformed
    //--------------------------------------------------------------------end

    //fungsi ketika matkul Strukdat ditekan
    //--------------------------------------------------------------------start
    private void jRadioButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButton2ActionPerformed
        jRadioButton1.setSelected(false);
        jRadioButton2.isSelected();
        matkulPilih = 2;
    }//GEN-LAST:event_jRadioButton2ActionPerformed
    //--------------------------------------------------------------------end

    //ketika menu exit diklik
    //--------------------------------------------------------------------start
    private void jMenu2MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jMenu2MouseClicked
        System.exit(0);
    }//GEN-LAST:event_jMenu2MouseClicked
    //--------------------------------------------------------------------end

    //fungsi tombol detail ketika ditekan
    //--------------------------------------------------------------------start
    private void detailsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_detailsButtonActionPerformed
        detailsPanel();;
    }//GEN-LAST:event_detailsButtonActionPerformed

    public static void main(String args[]) {

        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                UIManager.setLookAndFeel("com.jtattoo.plaf.smart.SmartLookAndFeel");

            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(tapClass.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }

        java.awt.EventQueue.invokeLater(
                () -> {
                    new tapClass().setVisible(true);
                }
        );
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.ButtonGroup buttonGroup2;
    private javax.swing.JButton checkButton;
    private javax.swing.JButton detailsButton;
    private javax.swing.JButton fingerButton;
    private javax.swing.JLabel fotoField;
    private javax.swing.JTextField inputField;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenu jMenu3;
    private javax.swing.JMenu jMenu4;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JMenuItem jMenuItem2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JRadioButton jRadioButton1;
    private javax.swing.JRadioButton jRadioButton2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel jamField;
    private javax.swing.JTable logTable;
    private javax.swing.JLabel logoFri;
    private javax.swing.JLabel persentaseField;
    private javax.swing.JButton tapButton;
    // End of variables declaration//GEN-END:variables
}
