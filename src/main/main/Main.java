package main;

import controllers.*;
import models.*;
import models.Module;
import sorting.MergeSort;
import sorting.OperatorComparators;

import java.util.Comparator;
import java.util.List;
import java.util.Scanner;

/**
 * Main file
 * 
 * @author Thiago Feijó de Albuquerque (https://github.com/F1Th-BR)
 */
public class Main {
    private static final Scanner scanner = new Scanner(System.in);
    private static final OperatorDatabase db = new OperatorDatabase(3);
    private static final OperatorExporter datExporter = new OperatorSerializer();
    private static final OperatorExporter mdExporter = new RosterExporter();
    private static final OperatorImporter importer = new OperatorSerializer();
    private static final MergeSort<Operator> sorter = new MergeSort<>();

    /* ===== ENTRY POINT ===== */

    public static void main(String[] args) {
        System.out.println("=== SISTEMA DE GERENCIAMENTO DE OPERADORES (ARKNIGHTS) ===");
        popularDadosIniciais();

        boolean rodando = true;
        while (rodando) {
            exibirMenu();
            System.out.print("\nEscolha uma opção: ");
            String opcao = scanner.nextLine().trim();

            switch (opcao) {
                case "1"  -> cadastrarNovoOperador();
                case "2"  -> buscarOperadorExato();
                case "3"  -> listarTodosOperadores();
                case "4"  -> buscarHabilidadePorPalavraChave();
                case "5"  -> removerOperador();
                case "6"  -> menuMelhorias();
                case "7"  -> menuModulos();
                case "8"  -> menuOrdenar();
                case "9"  -> menuExportar();
                case "10" -> menuImportar();
                case "0"  -> {
                    System.out.println("Encerrando o sistema. Até logo, Doutor!");
                    rodando = false;
                }
                default -> System.out.println("Opção inválida! Tente novamente.");
            }
        }
    }

    /* ===== MENU ===== */

    private static void exibirMenu() {
        System.out.println("\n================================================");
        System.out.println("[1]  Cadastrar Novo Operador");
        System.out.println("[2]  Buscar Operador por Nome (B-Tree)");
        System.out.println("[3]  Listar Todos os Operadores (In-Order)");
        System.out.println("[4]  Buscar Habilidades por Palavra-Chave (KMP)");
        System.out.println("[5]  Remover Operador");
        System.out.println("[6]  Menu de Melhorias (Elite / Nível / Skill / Maestria)");
        System.out.println("[7]  Menu de Módulos (Adicionar / Equipar)");
        System.out.println("[8]  Ordenar Roster (MergeSort)");
        System.out.println("[9]  Exportar (.dat / .md)");
        System.out.println("[10] Importar (.dat)");
        System.out.println("[0]  Sair");
        System.out.println("================================================");
    }

    /* ===== [1] CADASTRAR OPERADOR ===== */

    private static void cadastrarNovoOperador() {
        System.out.println("\n--- CADASTRO DE OPERADOR ---");
        System.out.print("Nome: ");
        String name = scanner.nextLine().trim();

        if (db.getOperatorExact(name) != null) {
            System.out.println("Operador '" + name + "' já está cadastrado.");
            return;
        }

        System.out.print("Classe (ex: Medic, Guard): ");
        String opClass = scanner.nextLine().trim();
        System.out.print("Subclasse (ex: Physician, Chain Medic): ");
        String opSubClass = scanner.nextLine().trim();
        System.out.print("Raridade (1-6): ");
        int rarity = lerInt();
        System.out.print("Elite (0/1/2): ");
        int elite = lerInt();
        System.out.print("Nível: ");
        int level = lerInt();
        System.out.print("Custo de DP: ");
        int dp = lerInt();
        System.out.print("Potencial (1-6): ");
        int potential = lerInt();

        Operator novo = new Operator.Builder(name, opClass)
                .operatorSubClass(opSubClass)
                .rarity(rarity)
                .elite(elite)
                .level(level)
                .dpCost(dp)
                .potential(potential)
                .build();

        System.out.print("Quantas habilidades deseja adicionar agora? ");
        int numSkills = lerInt();
        for (int i = 0; i < numSkills; i++) {
            System.out.println("  -- Habilidade " + (i + 1) + " --");
            System.out.print("  Slot (1/2/3): ");
            int slot = lerInt();
            System.out.print("  Nome: ");
            String sName = scanner.nextLine().trim();
            System.out.print("  Nível (1-7 ou M1/M2/M3): ");
            String sLevel = scanner.nextLine().trim();
            System.out.print("  Descrição: ");
            String sDesc = scanner.nextLine().trim();
            System.out.print("  Tipo (auto/manual/passive): ");
            String sType = scanner.nextLine().trim();
            System.out.print("  Recuperação (ex: Auto Recovery, Offensive Recovery): ");
            String sRec = scanner.nextLine().trim();
            novo.addSkill(new Skill(slot, sLevel, sName, sDesc, sType, sRec));
        }

        db.insertOperator(novo);
        System.out.println("Operador '" + name + "' inserido com sucesso.");
    }

    /* ===== [2] BUSCAR OPERADOR ===== */

    private static void buscarOperadorExato() {
        System.out.println("\n--- BUSCA EXATA (B-TREE) ---");
        System.out.print("Nome do operador: ");
        String name = scanner.nextLine().trim();

        Operator op = db.getOperatorExact(name);
        if (op != null)
            imprimirPerfilCompleto(op);
        else
            System.out.println("Operador \"" + name + "\" não encontrado.");
    }

    /* ===== [3] LISTAR TODOS ===== */

    private static void listarTodosOperadores() {
        System.out.println("\n--- ROSTER COMPLETO (IN-ORDER) ---");
        List<Operator> todos = db.inOrderValues();

        if (todos.isEmpty()) {
            System.out.println("Nenhum operador cadastrado.");
            return;
        }

        System.out.println(todos.size() + " operador(es) encontrado(s):\n");
        for (Operator op : todos)
            imprimirResumo(op);
    }

    /* ===== [4] BUSCAR POR PALAVRA-CHAVE ===== */

    private static void buscarHabilidadePorPalavraChave() {
        System.out.println("\n--- BUSCA POR PALAVRA-CHAVE (KMP) ---");

        String keyword = "";
        while (keyword.isEmpty()) {
            System.out.print("Termo a buscar (ex: Meltdown, Tactical): ");
            keyword = scanner.nextLine().trim();
        }

        List<Operator> resultados = db.searchBySkillKeyword(db.inOrderValues(), keyword);

        System.out.println("\n[Resultado para: '" + keyword + "']");
        if (resultados.isEmpty()) {
            System.out.println("Nenhuma correspondência encontrada.");
        } else {
            for (Operator op : resultados) {
                System.out.println("  " + op.getName() + " (" + op.getOperatorSubClass() + ")");
                for (Skill s : op.getSkills()) {
                    boolean nameMatch = !structures.KMP.KMPSearch(
                        s.getName().toLowerCase(), keyword.toLowerCase()).isEmpty();
                    boolean descMatch = !structures.KMP.KMPSearch(
                        s.getDescription().toLowerCase(), keyword.toLowerCase()).isEmpty();
                    if (nameMatch || descMatch)
                        System.out.println("    -> " + s.getName() + ": " + s.getDescription());
                }
            }
        }
    }

    /* ===== [5] REMOVER OPERADOR ===== */

    private static void removerOperador() {
        System.out.println("\n--- REMOÇÃO DE OPERADOR ---");
        System.out.print("Nome do operador a remover: ");
        String name = scanner.nextLine().trim();

        Operator op = db.getOperatorExact(name);
        if (op != null) {
            db.deleteOperator(name);
            System.out.println("Operador '" + name + "' removido com sucesso.");
        } else {
            System.out.println("Operador '" + name + "' não encontrado.");
        }
    }

    /* ===== [6] MENU DE MELHORIAS ===== */

    private static void menuMelhorias() {
        System.out.println("\n--- MELHORIAS ---");
        System.out.print("Nome do operador: ");
        String name = scanner.nextLine().trim();

        Operator op = db.getOperatorExact(name);
        if (op == null) {
            System.out.println("Operador não encontrado.");
            return;
        }

        System.out.println("  [1] Incrementar Elite");
        System.out.println("  [2] Incrementar Nível");
        System.out.println("  [3] Incrementar Potencial");
        System.out.println("  [4] Incrementar Skill Global (todas até nível 7)");
        System.out.println("  [5] Treinar Maestria Individual (7 → M1 → M2 → M3)");
        System.out.println("  [6] Definir Skill Padrão (default)");
        System.out.print("Tipo de melhoria: ");
        String tipo = scanner.nextLine().trim();

        switch (tipo) {
            case "1" -> {
                op.incrementElite();
                System.out.println("Elite atualizado para E" + op.getElite() + ".");
            }
            case "2" -> {
                System.out.print("Quantos níveis incrementar? ");
                int amount = lerInt();
                op.incrementLevel(amount);
                System.out.println("Nível atualizado para " + op.getLevel() + ".");
            }
            case "3" -> {
                System.out.print("Quantos potenciais incrementar? ");
                int amount = lerInt();
                op.incrementPotential(amount);
                System.out.println("Potencial atualizado para " + op.getPotential() + ".");
            }
            case "4" -> {
                db.upgradeGlobalSkills(name);
                System.out.println("Níveis de skill incrementados.");
            }
            case "5" -> {
                System.out.print("Slot da habilidade (1, 2 ou 3): ");
                int slot = lerInt();
                db.upgradeOperatorMastery(name, slot);
                System.out.println("Maestria do slot " + slot + " incrementada.");
            }
            case "6" -> definirSkillPadrao(op);
            default  -> System.out.println("Opção inválida.");
        }
    }

    private static void definirSkillPadrao(Operator op) {
        List<Skill> skills = op.getSkills();
        if (skills.isEmpty()) {
            System.out.println("Este operador não possui habilidades cadastradas.");
            return;
        }

        System.out.println("Habilidades disponíveis:");
        for (Skill s : skills)
            System.out.println("  Slot " + s.getSkillSlot() + ": " + s.getName() + " [" + s.getLevel() + "]");

        System.out.print("Slot da skill padrão: ");
        int slot = lerInt();

        boolean found = skills.stream().anyMatch(s -> s.getSkillSlot() == slot);
        if (found) {
            op.setDefaultSkillSlot(slot);
            System.out.println("Skill padrão definida: slot " + slot + ".");
        } else {
            System.out.println("Slot " + slot + " não encontrado.");
        }
    }

    /* ===== [7] MENU DE MÓDULOS ===== */

    private static void menuModulos() {
        System.out.println("\n--- MÓDULOS ---");
        System.out.print("Nome do operador: ");
        String name = scanner.nextLine().trim();

        Operator op = db.getOperatorExact(name);
        if (op == null) {
            System.out.println("Operador não encontrado.");
            return;
        }

        System.out.println("  [1] Adicionar Módulo");
        System.out.println("  [2] Equipar Módulo (definir padrão)");
        System.out.print("Opção: ");
        String opcao = scanner.nextLine().trim();

        switch (opcao) {
            case "1" -> adicionarModulo(op);
            case "2" -> equiparModulo(op);
            default  -> System.out.println("Opção inválida.");
        }
    }

    private static void adicionarModulo(Operator op) {
        System.out.println("  -- Novo Módulo --");
        System.out.print("  Nome: ");
        String mName = scanner.nextLine().trim();
        System.out.print("  Código de subclasse (ex: XAH, CCR, PHY): ");
        String subCode = scanner.nextLine().trim();
        System.out.print("  Tipo (X / Y / alpha): ");
        String modType = scanner.nextLine().trim();
        System.out.print("  Nível (1-3): ");
        int level = lerInt();
        System.out.print("  Descrição: ");
        String desc = scanner.nextLine().trim();

        op.addModule(new Module(mName, subCode, modType, level, desc));
        System.out.println("Módulo '" + mName + "' adicionado a " + op.getName() + ".");
    }

    private static void equiparModulo(Operator op) {
        List<Module> modules = op.getModules();
        if (modules.isEmpty()) {
            System.out.println("Este operador não possui módulos cadastrados.");
            return;
        }

        System.out.println("Módulos disponíveis:");
        for (int i = 0; i < modules.size(); i++)
            System.out.println("  [" + i + "] " + modules.get(i));

        System.out.print("Índice do módulo a equipar: ");
        int index = lerInt();

        if (index >= 0 && index < modules.size()) {
            op.setDefaultModuleIndex(index);
            System.out.println("Módulo equipado: " + modules.get(index).getName() + ".");
        } else {
            System.out.println("Índice inválido.");
        }
    }

    /* ===== [8] ORDENAR ===== */

    private static void menuOrdenar() {
        System.out.println("\n--- ORDENAR ROSTER (MERGESORT) ---");
        System.out.println("  [1] Por Nome (A → Z)");
        System.out.println("  [2] Por Raridade (6★ → 1★)");
        System.out.println("  [3] Por Nível (maior → menor)");
        System.out.println("  [4] Por Elite (E2 → E0)");
        System.out.println("  [5] Por Custo de DP (menor → maior)");
        System.out.println("  [6] Por Elite e Nível");
        System.out.println("  [7] Por Raridade e Nome");
        System.out.print("Critério: ");
        String opcao = scanner.nextLine().trim();

        Comparator<Operator> comparator = switch (opcao) {
            case "1" -> OperatorComparators.byName();
            case "2" -> OperatorComparators.byRarity();
            case "3" -> OperatorComparators.byLevel();
            case "4" -> OperatorComparators.byElite();
            case "5" -> OperatorComparators.byDpCost();
            case "6" -> OperatorComparators.byEliteThenLevel();
            case "7" -> OperatorComparators.byRarityThenName();
            default  -> null;
        };

        if (comparator == null) {
            System.out.println("Opção inválida.");
            return;
        }

        List<Operator> roster = db.inOrderValues();
        sorter.sort(roster, comparator);

        System.out.println("\n[Roster ordenado — " + roster.size() + " operador(es)]");
        for (Operator op : roster)
            imprimirResumo(op);
    }

    /* ===== [9] EXPORTAR ===== */

    private static void menuExportar() {
        System.out.println("\n--- EXPORTAR ---");
        System.out.println("  [1] Exportar .dat (persistência completa)");
        System.out.println("  [2] Exportar Roster .md (visualização)");
        System.out.print("Formato: ");
        String opcao = scanner.nextLine().trim();

        System.out.print("Caminho do arquivo: ");
        String path = scanner.nextLine().trim();

        List<Operator> todos = db.inOrderValues();

        switch (opcao) {
            case "1" -> {
                datExporter.export(todos, path);
                System.out.println(".dat exportado: " + path + " (" + todos.size() + " operador(es))");
            }
            case "2" -> {
                mdExporter.export(todos, path);
                System.out.println("Roster .md exportado: " + path + " (" + todos.size() + " operador(es))");
            }
            default -> System.out.println("Opção inválida.");
        }
    }

    /* ===== [10] IMPORTAR ===== */

    private static void menuImportar() {
        System.out.println("\n--- IMPORTAR .DAT ---");
        System.out.print("Caminho do arquivo: ");
        String path = scanner.nextLine().trim();

        List<Operator> imported = importer.importData(path, db, (conflicting) -> {
            System.out.println("Operador '" + conflicting.getName() + "' já existe.");
            System.out.print("  [1] Sobrescrever  [2] Pular: ");
            String choice = scanner.nextLine().trim();
            return choice.equals("1")
                ? ConflictResolution.OVERWRITE
                : ConflictResolution.SKIP;
        });

        System.out.println(imported.size() + " operador(es) importado(s) com sucesso.");
    }

    /* ===== SEEDER ===== */

    private static void popularDadosIniciais() {
        Operator kaltsit = new Operator.Builder("Kal'tsit", "Medic")
                .operatorSubClass("Physician")
                .rarity(6)
                .elite(2)
                .level(60)
                .dpCost(20)
                .potential(1)
                .build();
        kaltsit.addSkill(new Skill(1, "7", "Command: Structural Fortification", "Def Up", "manual", "Auto Recovery"));
        kaltsit.addSkill(new Skill(2, "7", "Command: Tactical Coordination", "Attack Speed Up for Mon3tr", "manual", "Auto Recovery"));
        kaltsit.addSkill(new Skill(3, "M3", "Command: Meltdown", "Mon3tr deals massive True Damage", "manual", "Auto Recovery"));
        kaltsit.setDefaultSkillSlot(3);
        kaltsit.setDefaultModuleIndex(0);

        Operator mon3tr = new Operator.Builder("Mon3tr", "Medic")
                .operatorSubClass("Chain Medic")
                .rarity(6)
                .elite(2)
                .level(60)
                .dpCost(18)
                .potential(1)
                .build();
        mon3tr.addSkill(new Skill(1, "7", "Stratagem: Hyperpressurized Link", "Chain heal extension", "auto", "Auto Recovery"));
        mon3tr.addSkill(new Skill(2, "7", "Stratagem: Overload", "Activates Tactical Coordination ASPD talent effect", "manual", "Offensive Recovery"));
        mon3tr.addSkill(new Skill(3, "M3", "Stratagem: Meltdown", "Overloads the chain core dealing energy splash", "manual", "Auto Recovery"));
        mon3tr.setDefaultSkillSlot(3);

        db.insertOperator(kaltsit);
        db.insertOperator(mon3tr);
        System.out.println("Dados iniciais carregados: Kal'tsit, Mon3tr.\n");
    }

    /* ===== DISPLAY HELPERS ===== */

    /**
     * Prints a compact single-line summary of an operator.
     * Used in list and sort views.
     */
    private static void imprimirResumo(Operator op) {
        Skill ds = op.getDefaultSkill();
        Module dm = op.getDefaultModule();
        System.out.printf("  %-20s | %d★ | E%d Lv%-3d | DP: %-3d | Skill: %-6s | Module: %s%n",
            op.getName(),
            op.getRarity(),
            op.getElite(),
            op.getLevel(),
            op.getDpCost(),
            ds != null ? "S" + ds.getSkillSlot() + "-" + ds.getLevel() : "-",
            dm != null ? dm.getSubClassCode() + "-" + dm.getModType() : "-"
        );
    }

    /**
     * Prints the full profile of an operator including all skills and modules.
     * Used in the exact search view.
     */
    private static void imprimirPerfilCompleto(Operator op) {
        System.out.println("\n[PERFIL: " + op.getName() + "]");
        System.out.println("  Raridade : " + op.getRarity() + "★");
        System.out.println("  Classe   : " + op.getOperatorClass() + " / " + op.getOperatorSubClass());
        System.out.println("  Elite    : E" + op.getElite() + " | Nível: " + op.getLevel());
        System.out.println("  DP Cost  : " + op.getDpCost());
        System.out.println("  Potencial: " + op.getPotential());

        List<Skill> skills = op.getSkills();
        System.out.println("  Habilidades (" + skills.size() + "):");
        if (skills.isEmpty()) {
            System.out.println("    - Nenhuma.");
        } else {
            for (Skill s : skills) {
                String tag = s.getSkillSlot() == op.getDefaultSkillSlot() ? " [PADRÃO]" : "";
                System.out.println("    Slot " + s.getSkillSlot() + tag + ": " + s.getName() + " [" + s.getLevel() + "]");
                System.out.println("      Desc: " + s.getDescription());
                System.out.println("      Tipo: " + s.getSkillType() + " | Rec: " + s.getRecovery());
            }
        }

        List<Module> modules = op.getModules();
        System.out.println("  Módulos (" + modules.size() + "):");
        if (modules.isEmpty()) {
            System.out.println("    - Nenhum.");
        } else {
            for (int i = 0; i < modules.size(); i++) {
                Module m = modules.get(i);
                String tag = i == op.getDefaultModuleIndex() ? " [EQUIPADO]" : "";
                System.out.println("    [" + i + "]" + tag + " " + m.getSubClassCode() + "-" + m.getModType() +
                                   " Lv" + m.getLevel() + ": " + m.getName());
                System.out.println("      Desc: " + m.getDescription());
            }
        }
    }

    /* ===== UTIL ===== */

    private static int lerInt() {
        while (true) {
            try {
                return Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.print("  Valor inválido, tente novamente: ");
            }
        }
    }
}