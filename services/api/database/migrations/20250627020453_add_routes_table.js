/**
 * @param { import("knex").Knex } knex
 * @returns { Promise<void> }
 */
exports.up = function(knex) {
    return knex.schema
        .createTable('routes', function (table) {
            table.increments('id').primary();
            table.string('route').notNullable();
            table.integer('environment_id').unsigned().notNullable();

            table.foreign('environment_id').references('id').inTable('environment').onDelete('CASCADE');
        });
};

/**
 * @param { import("knex").Knex } knex
 * @returns { Promise<void> }
 */
exports.down = function(knex) {
    return knex.schema.dropTableIfExists('routes'); 
};
