# trabalho-busca

Para compilar o programa:

1. Clonar este repositório para a máquina local
2. Navegar até o diretório
3. Executar `./gradlew distZip`

Na pasta build/distributions será gerado um .zip contendo o programa compilado.

# uso do programa

Passando argumentos na forma `$./trabalhoia width height pipes`, onde os 3 argumentos são inteiros,
o programa tentará resolver o problema especificado utilizando o algoritmo A*.

Exemplo: `./trabalhoia 4 3 3`

Passando um argumento na forma `$./trabalhoia algoritmo`, onde algoritmo pode ser 'a', 'd' ou 'b',
A*, profundidade e largura, respectivamente, o programa tentará resolver os problemas do tipo
`(n, m, k)` onde `n = m = k = i` e `i = 2..6`, utilizando o algoritmo especificado. Timeout de 1 minuto para cada iteração.

Exemplo: `./trabalhoia a`
