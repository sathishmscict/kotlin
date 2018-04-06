interface Am {
    String <caret>getIcon();
}

class AIIm implements Am {
    @Override
    public String getIcon() {
        return null;
    }

    public static void main(String[] args) {
        new AIIm().getIcon();
    }
}