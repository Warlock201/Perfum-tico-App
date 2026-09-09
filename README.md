# Perfumático (Android)

Aplicativo Android nativo desenvolvido em **Kotlin** e **Jetpack Compose** para gerenciamento inteligente de acervo de perfumes, notas olfativas e recomendações.

## Funcionalidades
- **Meus Perfumes**: Gestão de frascos com abas "TENHO", "VOU COMPRAR" e "QUERO", filtros por ocasião/tag e família olfativa.
- **Ficha Pessoal & Pirâmide Olfativa**: Avaliação de fixação (1-10), projeção (1-10), notas de saída, coração e fundo, volumes e impressões pessoais.
- **Dashboard Financeiro & Estatísticas**: Cálculo de patrimônio estimado (Min / Max em R$), metas de compras, distribuição do acervo e médias de performance.
- **Descubra (Recomendações)**: Algoritmo de sugestão baseado no perfil do colecionador (família preferida e notas mais usadas) além de explorador manual.
- **Catálogo Geral & Notas Olfativas**: Base com mais de 200 fragrâncias e catálogo de notas olfativas de referência.
- **Scent of the Day (SOTD)**: Registro do perfume do dia com histórico e perfil do colecionador.

## Arquitetura & Tecnologias
- **UI**: Jetpack Compose com Material Design 3 (Dark Luxury Theme).
- **Persistência Local**: Room Database com Kotlin Coroutines e Flow.
- **Imagens**: Coil Compose.
- **Build System**: Gradle com Kotlin DSL.

