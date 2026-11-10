package com.brendigo.brojko;

import android.graphics.Path;
import android.util.Base64;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.zip.InflaterInputStream;

/**
 * Native vector outlines traced from user-provided Brojko 941x1672 owner
 * references. Each segment stores closed polygons, including interior
 * counters (Path.FillType.EVEN_ODD). No screenshots, backgrounds, fixed
 * gameplay values or raster images are packed in the application.
 *
 * The numbers are compressed geometry, not pixel bitmaps. They are used
 * only for the four screen headings and five static section captions where Nunito's actual
 * glyph contours diverge from the supplied logo-style typography.
 */
final class OwnerHeadlineContours {
    private OwnerHeadlineContours() {}

    static final int MISSIONS_NAVY = 0;
    static final int MISSIONS_CORAL = 1;
    static final int THEMES_NAVY = 2;
    static final int THEMES_CORAL = 3;
    static final int STATS_NAVY = 4;
    static final int SETTINGS_NAVY = 5;
    static final int STATS_CHART = 6;
    static final int THEMES_STYLE = 7;
    static final int MISSIONS_DAILY = 8;
    static final int MISSIONS_WEEKLY = 9;
    static final int MISSIONS_ACHIEVEMENTS = 10;

    private static final String[] DATA = {
        "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",
        // STATS_CHART: static Croatian source text, not application data.
        "eNod1PtPFWQcx/Hn2bMAdUYoBbT8RecFPChXDwhHLgckqPwh8RaQQc25uRbNXKz0B5vZaqNpLSr4wab8YEWzNlv+B2Tl2mxzTitjXVjhP2At6yU/vM/zPd/n870957sTHg4VcVeqjTtTfexPjXE39qSGOIChtAXZOJiaaJrik6kF7TT5+BSGUkfc73wGI+wR+mHxw3Ltl3MIg6lGvppQKCoX98owiKHUipwqbXxtYWl8nK6Pvk+u7tSDR2NP6vO9z11v3JG2o1aePGU7ZXvqjB3U+cWIDtSF4phxW4X1qS6uda7BaurV1Gso12Ide5151uu3ypzVi9SG0vgAZbHIYlOU8VZQVHiBcuoSFKfmuFzf93uFEpkeQplsjzhXOVeJKVdxJVYsdrsibUMulpq6HBUmLzP1SlOXxvvootf5T+//OO8476SG8HdqdGbRw98X/sXd1Is8baeYvNh8XGLuZbIv0+dScUVuGmJCga4L1EpqRbXuplZkQ2E4q84ZvZ1JneEj+c5iOrWjLiwPH7p9HxMUE6krvJO2oze8q4MJfMCe1NVk6g5T7qdSfXgwHKc+Lt9rcp3AydSBlvB62oqm8IZ53tLfuA7G6d6mOYXT7NP8p9QeV/VNnGSfcJaGfax9Iocpnk1bkA0jsg1gT8qFvboekMX+0XSG53CAfZD/IP0B8SMyPQ27GIrCYNqGHE+LmxYZW5ET3UbRFspiY9oY61MGm2ONbajxlnWotxH13rLBi2e9fAty7JxNaOXfiia/RBNdM30rcjapxS/cjGzKhILYm6qQsdXVtvkelexKLzjn86e0MdxMm1ATbuj3hg5v6vBH8/xstl8wZ77f8acXXtD5As1tr3zbrAti/krVYT5V0VSGJdSbwm+YdztP+YeIX8055x1u+X7r3taH72lnxc2q+zXPN/iWfcV5RcR31Jcxq49ZPVxW/xqu6+E63zV3P+Aq7VW1C+1WJTL2qzqcwzR72s05/gJ7VBneM+uEm0lMuZnkKwndPrt86+Lt1vcT2JE2h7ye86p1q9qP3ex+FR9Dn6r9NLtk3Cm+KHamDf4dqpDxD1EdZfPvkEGVf4sNXvs83XmTfCzXp5hhz9iqCzbtC1y0aRf5vnT3FS6xL9n0i17lc1zwMp9hhj0j1yeyloRXfY75Nsb7stsXnS84D9nFQ7p8Xtejsh3BGPsV51GZj7o75nc+5pWKwmH1j2BMD2O8L5n9MEbZoyn7P0iO8a8=",
        // THEMES_STYLE: static Croatian source text, not application data.
        "eNodkv1LU1EYx895Htx0kGUTjH6RUHEoiFCSIQbSdt1W3rXNu7u77e4OmuUPBsNCaNA7GaF/5qxmUEFKb7D6sB8+nHOfl+/3POceM2Gm5KW68kILUJJ3WpT3WoZtOVSPnCfP1YcG+aa80kjewqGG1ITygf0x8WN6jug/0vvEXHIuNVtmVF5rDQK6fCI+mSoEONXljdZNUp7oluxT34HH+O+htUdXB9199A+gi1eX2FO6OlphLRIvyTPosu9y/gP8YrKDWhulNt0P6dxl3VXXXJGATJVMBUr0FTnJNrpVKgIqGxCyj6BFvIVXhGcIDfZV1go9Fe6kSn8N34i1xcwNvC9IGoc7qG/AbbLrTLLOrBsobqKeZc2Di5JLZ55zjUqGyuzwywOfyjpKdbp88MwlWUA3BbNMeY11Gv1p3ZGZIU3ioczBPB4LsMR+Gd9l/Je0gMc8HilY5L8uMkMK/TlOMYPHrJZNXMbQjUMM7RiacXQSrAnOOGX/atb+1jzcsz/VhaL9BX+0DA07oNqC4qy8FUHfaJ14ACX7D28BZQKLi0FjgNZAc+aq7atjTzRne1qwH6n+qp79rj4E9pvW7Gf4xP4Eeux7OJ5qRF1kz9ifEzun5we9p2j0Ue+j/oUTn+ldyKLlMGVbM/JAN1lzvI48LyMHjjzSjEnICtkbfF3nZlaY/RYTrTHRGhOtcp+r5G4OKy9SOc7XONkJqpIwScckcyfpTuJxmcq4jFA1olnuM89d5sDhrjPcccaMiccuIFOHJplQ07ylNG/LIe6Ir+n/mK+qMg==",
        // MISSIONS_DAILY: static Croatian source text, not application data.
        "eNoNkEtLW1EUhdcqLYmIA4UiMcXa+hj5SyxFvLWJeRg1xqh/SGwLCgYU2ooDHWihkEx0YAbFKm3jIyKiiKhUtFWxj2/w3bvP3mufvfZRWE8cceDH7nWDE5ByvdMwSDwEw9RG0Yz7GbR6zB3Ouc1Zzlm3UI9CBG2EniYP+Dm0OuN26HA/JIj7IIY+UBhlChJ0xt0MLcRPyUWhyWl16sC9OnJWJx7Tqcd1Dj+d1yW5Kw9Av345CXHd+LVuHYNR3aP7Q88/fMp5SPuBk34EIbyE2KWGPer41+Gplr3D7P8QTCzm/sXjHdwQ/4Zr4ku4YNaFEzpj9ikuT6gdkz+EqL74lcooyijWcbaO07Jz2sBRBbZxtQtVtqh6WHvUt6FCvAVfPQSD2mS7Hdh1Bl1G+9xV5c49tqwwaQNCfJMo4/oGP6h+d4pbUuTSatQSmkX8LPIiS8xchhXiFZx/Rl1EWWReiflFZpfwWUKzCmvo1tCsug9i5AN6An1i22X+9XrnLr31C03ChF9CXm/YsAAfPAI5zXPzAu4XcP3R3XqPbhZ9AWboD2vaPZwCzTFjDl8F/EzDFG8w5eA/AvuZ4w==",
        // MISSIONS_WEEKLY: static Croatian source text, not application data.
        "eNoNkktPU1EUhddxKdRbhRK0DnQCBjFtaW0BeSoPMRH+n3EiDMQohQgkyABIlFZCjRETaqIoQcoABjowJJqIicRv8N279vvse64iXdIPp/XdGRhGj+gnHHsIuvTbBf3xbRjWCf5/cIo+dR59C7L6S/0vOHYHtWk16CXPRVig6wIVLzymWZjxPT3zfT33uIqeIGdCS36gZWLL5F4LF5wKcadDvTPhrHPB7gxn3AV94ZyHQh3EPBIiiHs0JCCJL+mB0Ox+7MFwEeLYETVx94ZGSLgnXIYr9Eu6gM6Sn1F9OI8ncp6qXGiAJqIJJkZsf1VvnFKZTV6Ru+acVt2pFSIr7sO+o9dsV4KyR/UWtvhuWx7QO/er4kFtQBm7RH7ZvdqEinv0Hj64GwroLPkZ1Wkda4MpFb5uhUmbTCpBQo98XQ/dBjfRKT2GSc41yV1MMf8JTKOn8T0lVoQ5cueoKVKb1KFbdQA1rBrRfTI/Q5VZVWZtM2ebU1Y5+Uff1Sc6foGvbLXL+xvsM+GA9yEcoY/oFSOa1x7U6FBjrz323YUddt3BF+OeWzRP7jynWXQ7/0c7930DWtEt/wFY4IPb",
        // MISSIONS_ACHIEVEMENTS: static Croatian source text, not application data.
        "eNodk+1Lk2EUh8/R5+zZdNPN6dx0oUiIFWWJla5EW0RpRUlg/WcWNO0F+5BSH7KitA+F0ouWxig0oomMFGz0Qmm5Tbvqw7Xn99znnN85594mQYlpr6metFLw6ClztQ/OmOk56Ef3m1fPm09PQ5+VkVOmJywAYXSEvAjxGh3gecmqwdWLeA1YiV4woU7EJSOAUwUE9axVcRqESnSAGQ4zQxt1bXgfsHKeIW3Hvx2/Dnx74DjvSSqTVPZQ2c0zAR24djBbgvoeSDJ3kn2O4dlN/wR0mkqt7kDVE6k3v8aZJ45bI87N0EKnXbAHvQ/2o1uJt5K/l7rd0MJGLdQ1030nNKGbOGtkw7C6ZHh481DpxaEc/NyLn30qmSsEYfYLE68hFiMW455qqammNggBdDluUS3hcxu2mLzAcx2+myPfzAM++WplvIfkp1VLHgoWliLPokVk02Lyw6Lk1UFcflkD8QZVi2upRUHFK79xykPRvOBHV3BWQSc/eMlYpecKuStWg47IGuTokbNStEhIHvB5n4x7cNdM7pgLQXSVjJM9AU+Z8plVghftgskT8ifgMfVeXFyUK5PsNMnv4REOD2EcPc6udfLK2ZZpK5EpPF4w3wyR13jO4P2SzGmYQk8x3XN6zkGaG0mTO4/DLHUz7DpL7zmYR6e5zTSeb5lhDt7QIyofnE1ZcIqwLe+Z8N1/TBaYb4GOi3RaxPkjHZYhy91kOVuCDPEMeUu4L0OWDlk6ZPDKOAX5hHdIRpw/cgUGnbwMUn0Zl6uQQqdwHuLuhvgehtnxFvOPMvsobmNMMcact3HyyTVnS67jexNGmHGEXjfoPQwpMlP//nWy6mzIZ7qustEa5OiZo/cXzn3a5WzoUWddE3DQyUNBDzlF7YQj6C5n8y+ptrk4"
    };

    static Path decode(int index) {
        if (index < 0 || index >= DATA.length) {
            throw new IllegalArgumentException("Unknown headline " + index);
        }
        try (DataInputStream source = new DataInputStream(
                 new InflaterInputStream(new ByteArrayInputStream(
                     Base64.decode(DATA[index], Base64.NO_WRAP))))) {
            int contours = source.readUnsignedShort();
            if (contours > 100) throw new IOException("Too many outlines");
            Path path = new Path();
            path.setFillType(Path.FillType.EVEN_ODD);
            for (int i = 0; i < contours; i++) {
                int vertices = source.readUnsignedShort();
                if (vertices < 3 || vertices > 2048) {
                    throw new IOException("Invalid outline size");
                }
                for (int j = 0; j < vertices; j++) {
                    float x = source.readUnsignedShort();
                    float y = source.readUnsignedShort();
                    if (j == 0) path.moveTo(x, y);
                    else path.lineTo(x, y);
                }
                path.close();
            }
            return path;
        } catch (IOException ex) {
            throw new IllegalStateException("Invalid packaged owner contours", ex);
        }
    }
}
