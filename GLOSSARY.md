# Agentic Keycloak Lab

A small shop where Customers order Products from a Catalog, overseen by Admins. It exists to practise agentic engineering on a realistic, secured, distributed system.

## Language

### Shop

**Catalog**:
The fixed set of Products that can be ordered, each with its Price.
_Avoid_: inventory, store, shop

**Product**:
An item in the Catalog that can be ordered.
_Avoid_: item, article, SKU

**Price**:
What one unit of a Product costs, in euros.
_Avoid_: cost, rate

**Order**:
A Customer's request to buy a quantity of one Product, fixed at the Price the Catalog quoted when it was placed. An Order never changes once placed.
_Avoid_: purchase, transaction, basket, cart

**Total**:
What an Order costs: its quantity times the Price it was placed at.
_Avoid_: amount, sum, subtotal

### People

**Customer**:
A person who places Orders and can see only their own.
_Avoid_: user, buyer, client

**Admin**:
A person who can see every Order. Being an Admin does not make someone a Customer.
_Avoid_: administrator, superuser, manager
