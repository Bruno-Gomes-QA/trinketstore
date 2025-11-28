# Visão de Negócio & Marketing · Trinket Store

## Essência do produto
Trinket Store é um e-commerce verticalizado para itens colecionáveis e presentes autorais. O sistema entrega vitrine premium, checkout instantâneo via PIX e um cockpit administrativo que permite ao time operar catálogo, estoque, preços e pedidos em tempo real sem depender de múltiplas plataformas.

## Promessa de valor
- **Compra sem atrito**: login com Google, carrinho guiado em três passos e QR Code PIX dinâmico que vence junto com o pagamento.
- **Transparência para o cliente**: área "Meus pedidos" rastreia produção, pagamento e retirada com atualizações automáticas.
- **Operação enxuta**: dashboard unifica métricas de vendas, estoque baixo e pedidos recentes para decisões rápidas.
- **Segurança e confiança**: integrações oficiais com Mercado Pago e Supabase, notificações auditáveis e logs de webhook.

## Experiência do cliente final
1. **Descoberta** – vitrine Nuxt responsiva destaca coleções, estoque disponível e storytelling dos produtos.
2. **Seleção & carrinho** – componentes interativos mostram preço unitário, disponibilidade e alertas de estoque em tempo real.
3. **Identificação** – login social Google evita formulários extensos e liga cada pedido a um perfil verificado.
4. **Pagamento PIX** – botão "Gerar PIX" cria QR Code + copia-e-cola, cronômetro de expiração e confirmação automática assim que o banco aprova.
5. **Pós-venda** – página `/pedidos` lista histórico, permite cancelar antes da produção e mantém o cliente informado sem precisar chamar o suporte.

## Experiência do time Trinket
- **Dashboard executivo** (`/sistema/dashboard`) traz KPIs imediatos: produtos ativos, pedidos pendentes/pagos/para retirada e alertas de estoque baixo.
- **Gestão de catálogo** – telas internas permitem cadastrar produtos, preços e imagens mantendo consistência entre vitrine e estoque.
- **Controle de inventário** – nuvem única de estoque evita overbooking e destaca itens próximos do fim.
- **Workflow de pedidos** – equipe acompanha toda a jornada (pending → paid → fulfilled/picked_up) e pode intervir manualmente quando necessário.
- **Auditoria** – logs de webhooks + registros Flyway garantem rastreabilidade para fins fiscais e de compliance.

## Diferenciais competitivos
- **Checkout proprietário**: experiência PIX personalizável sem redirecionamentos, favorecendo branding e upsell.
- **BFF em Nuxt**: garante latência baixa ao servir vitrine e sistemas internos a partir do mesmo deploy.
- **Stack moderna**: Nuxt 3 + Tailwind + Spring Boot 3 posicionam a loja para crescer com base sólida e time-to-market curto.
- **Experiência omnichannel**: painel permite alternar rapidamente entre vendas online, retirada física e campanhas especiais sem retrabalho manual.

## Pitch rápido
> "Trinket Store é o e-commerce autoral para marcas que precisam de um checkout PIX impecável e um cockpit unificado para vendas e estoque. Em poucas horas o time coloca novos produtos no ar, acompanha pedidos em tempo real e mantém o cliente informado do botão comprar até a retirada."

## Próximos movimentos sugeridos
1. **Campanhas de aquisição**: promover login com Google + checkout instantâneo como diferencial em anúncios e landing pages.
2. **Retargeting pós-PIX**: usar eventos do webhook para disparar e-mails/WhatsApp automáticos quando o pagamento cai.
3. **Programa de fidelidade**: aproveitar identificações Supabase para acumular pontos e oferecer perks exclusivos.
4. **Integração omnichannel**: conectar estoque a marketplaces físicos para ampliar alcance sem duplicar cadastros.
