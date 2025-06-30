package net.IneiTsuki.regen.magic;

public class MagicEnums {

       public enum Clarification {
            CONTROL("control", 0x4A90E2),      // Blue
            DESTRUCTION("destruction", 0xE24A4A), // Red
            CONSTRUCTION("construction", 0x4AE24A), // Green
            MUCH("much", 0xE2A04A),           // Orange
            LITTLE("little", 0xA04AE2),       // Purple
            SOME("some", 0xE2E24A),           // Yellow
            MANY("many", 0x4AE2A0),           // Cyan
            AREA("area", 0xE24AA0),           // Pink
            MOVE("move", 0x808080);           // Gray

            private final String name;
            private final int color;

            Clarification(String name, int color) {
                this.name = name;
                this.color = color;
            }

            public String getName() {
                return name;
            }

            public int getColor() {
                return color;
            }

            public String getFormattedName() {
                return name.substring(0, 1).toUpperCase() + name.substring(1);
            }
       }

       public enum MagicType {
            TARGET("target", 0x800080),       // Purple
            LIGHT("light", 0xFFFF80),         // Light Yellow
            DARK("dark", 0x404040),           // Dark Gray
            WATER("water", 0x4080FF),         // Blue
            LIFE("life", 0x80FF80),           // Light Green
            FIRE("fire", 0xFF4040),           // Red
            AIR("air", 0xC0C0C0),            // Light Gray
            ICE("ice", 0x80FFFF),            // Cyan
            EARTH("earth", 0x8B4513);         // Brown

            private final String name;
            private final int color;

            MagicType(String name, int color) {
                this.name = name;
                this.color = color;
            }

            public String getName() {
                return name;
            }

            public int getColor() {
                return color;
            }

            public String getFormattedName() {
                return name.substring(0, 1).toUpperCase() + name.substring(1);
            }
       }
}
