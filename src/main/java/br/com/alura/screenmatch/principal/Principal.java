package br.com.alura.screenmatch.principal;

import br.com.alura.screenmatch.model.DadosEpisodio;
import br.com.alura.screenmatch.model.DadosSerie;
import br.com.alura.screenmatch.model.DadosTemporada;
import br.com.alura.screenmatch.model.Episodio;
import br.com.alura.screenmatch.service.ConsumoApi;
import br.com.alura.screenmatch.service.ConverteDados;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

public class Principal {

    private Scanner leitura = new Scanner(System.in);
    private final String URL = "https://www.omdbapi.com/?t=";
    private final String APIKEY = "&apikey=6585022c";

    public void exibeMenu(){
        System.out.println("Digite o nome da série para busca: ");
        var nomeSerie = leitura.nextLine();

        ConsumoApi consumoApi = new ConsumoApi();
        var json = consumoApi.obterDados(URL + nomeSerie.replace(" ", "+") + APIKEY);

        ConverteDados conversor = new ConverteDados();
        DadosSerie dados =  conversor.obterDados(json, DadosSerie.class);
        System.out.println(dados);

//        System.out.println("Digite qual temporada de busca: ");
//        var temporadas = leitura.nextLine();

        List<DadosTemporada> temporadas = new ArrayList<>();

        for (int i = 1; i <= dados.totalTemporadas(); i++) {
            json = consumoApi.obterDados(URL + nomeSerie.replace(" ", "+") + "&season=" + i + APIKEY);
            DadosTemporada dadosTemporada =  conversor.obterDados(json, DadosTemporada.class);
            temporadas.add(dadosTemporada);
        }
//        temporadas.forEach(System.out::println);

//        temporadas.forEach(t -> t.episodios().forEach(e ->System.out.println(e.titulo())));

//        for (int i = 0; i < dados.totalTemporadas(); i++) {
//            List<DadosEpisodio> episodiosTemporada = temporadas.get(i).episodios();
//            for (int j = 0; j < episodiosTemporada.size(); j++) {
//                System.out.println("S="+ (i+1) +" E="+(j+1)+" - "+episodiosTemporada.get(j).titulo());
//            }
//        }

        List<DadosEpisodio> dadosEpisodios = temporadas.stream()
                .flatMap(t -> t.episodios().stream())
                .collect(Collectors.toList());



        dadosEpisodios.stream()
                .filter(e -> !e.avaliacao().equalsIgnoreCase("N/A"))
                .sorted(Comparator.comparing(DadosEpisodio::avaliacao).reversed())
                .limit(5)
                .forEach(e -> System.out.println(e));



        List<Episodio> episodios = temporadas.stream()
                .flatMap(t -> t.episodios().stream()
                        .map(e -> new Episodio(t.numero(), e))
                )
                .collect(Collectors.toList());

        episodios.forEach(System.out::println);

        System.out.println("A partir de que ano voce quer ver os epsodios");
        var ano = leitura.nextInt();
        leitura.nextLine();

        LocalDate dataBusca = LocalDate.of(ano, 1,1);

        episodios.stream()
                .filter(episodio ->
                        episodio.getDataLancamento() != null
                        && episodio.getDataLancamento().isAfter(dataBusca)
                ).forEach(System.out::println);

    }
}
