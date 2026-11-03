# Relational Join Table for Hops and Beer Styles

We modeled the many-to-many relationship between hop varieties and beer style guidelines using a dedicated Room join entity (`HopStyleCrossRef`) indexed on `(hopId, styleId, guidelineType)` rather than embedding style ID strings or JSON arrays inside the hop entity. This enables sub-millisecond, bidirectional indexed queries so hop detail screens can instantly resolve associated beer styles, and beer style detail screens can query and display featured hops without runtime text parsing or full-table scans.
